/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JavaMailSenderAroundAdviceTest {

    @Mock
    private MailStore mailStore;

    @Mock
    private List<OutgoingMail> mails;

    @Mock
    private MimeMessage message;

    @Mock
    private ProceedingJoinPoint pjp;

    @InjectMocks
    private JavaMailSenderAroundAdvice advice = new JavaMailSenderAroundAdvice();
    
    @Before
    public void setUp() throws Exception {
        when(mailStore.getMails()).thenReturn(mails);
        when(message.getFrom()).thenReturn(new Address[] {new InternetAddress("from")});
        when(message.getAllRecipients()).thenReturn(new Address[] {new InternetAddress("to")});
        when(message.getSubject()).thenReturn("subject");
        when(message.getContent()).thenReturn("body");
    }

    @Test
    public void testAroundAdviceInterceptsAndStoresMessageWhenMailServerNotSet() throws Throwable {
        advice.setMailHost(null);
        Object[] args = new Object[] {new Object(), message, new Object()};
        when(pjp.getArgs()).thenReturn(args);
        advice.interceptMailSending(pjp);
        verify(mails).add(new OutgoingMail(message));
        verifyNoMoreInteractions(mails);
        verify(pjp, never()).proceed();
    }

    @Test
    public void testAroundAdviceDoesNothingWhenMailServerSet() throws Throwable {
        advice.setMailHost("mailhost");
        advice.interceptMailSending(pjp);
        verify(pjp).proceed();
        verifyNoMoreInteractions(pjp, mails);
    }
}
