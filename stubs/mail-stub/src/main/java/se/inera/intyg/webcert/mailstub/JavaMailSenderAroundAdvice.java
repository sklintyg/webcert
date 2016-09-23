/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.mailstub;

import java.util.stream.Collectors;

import javax.mail.internet.MimeMessage;

import org.apache.cxf.common.util.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author andreaskaltenbach
 */
@Aspect
public class JavaMailSenderAroundAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(JavaMailSenderAroundAdvice.class);

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
                    OutgoingMail outgoingMail = new OutgoingMail((MimeMessage) argument);
                    mailStore.getMails().add(outgoingMail);

                    LOG.info("\n*********************************************************************************\n"
                            + " Intercepting mail to : '{}' subject: '{}' from: '{}'.\n"
                            + "{}\n"
                            + "*********************************************************************************", outgoingMail.getRecipients().stream().collect(Collectors.joining(", ")), outgoingMail.getSubject(), outgoingMail.getFrom(), outgoingMail.getBody());

                    mailStore.waitToContinue();
                }
            }
            return null;
        } else {
            return pjp.proceed();
        }
    }
}
