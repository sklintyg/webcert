package se.inera.webcert.mailstub;

import javax.mail.internet.MimeMessage;

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

    /**
     * Intercepts and mail sending calls and stores the mime message in the MailStore.
     */
    @Around("execution(* org.springframework.mail.javamail.JavaMailSender+.send(..))")
    public Object interceptMailSending(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("In around advice");

        for (Object argument : pjp.getArgs()) {
            if (argument instanceof MimeMessage) {
                mailStore.getMails().add(new OutgoingMail((MimeMessage) argument));
            }
        }

        return null;
    }
}