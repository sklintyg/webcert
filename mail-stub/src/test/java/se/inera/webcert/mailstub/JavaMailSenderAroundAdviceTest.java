package se.inera.webcert.mailstub;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

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

    private JavaMailSenderAroundAdvice advice;
    
    @Before
    public void setUp() throws Exception {
        advice = new JavaMailSenderAroundAdvice();
        when(mailStore.getMails()).thenReturn(mails);
        advice.mailStore = mailStore;
        when(message.getAllRecipients()).thenReturn(new Address[] {});
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
