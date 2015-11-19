package se.inera.intyg.webcert.mailstub;

import javax.mail.internet.MimeMessage;

import org.apache.cxf.common.util.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author andreaskaltenbach
 */
@Aspect
public class JavaMailSenderAroundAdvice {

    @Autowired
    private MailStore mailStore;

    private String mailHost;

    public String getMailHost() {
        return mailHost;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    /**
     * Intercepts and mail sending calls and stores the mime message in the MailStore.
     */
    @Around("execution(* org.springframework.mail.javamail.JavaMailSender+.send(..))")
    public Object interceptMailSending(ProceedingJoinPoint pjp) throws Throwable {
        if (StringUtils.isEmpty(mailHost)) {
            for (Object argument : pjp.getArgs()) {
                if (argument instanceof MimeMessage) {
                    mailStore.getMails().add(new OutgoingMail((MimeMessage) argument));
                    mailStore.waitToContinue();
                }
            }
            return null;
        } else {
            return pjp.proceed();
        }
    }
}
