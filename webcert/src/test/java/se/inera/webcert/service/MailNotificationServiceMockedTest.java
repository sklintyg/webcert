package se.inera.webcert.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import se.inera.ifv.hsawsresponder.v3.GetHsaUnitResponseType;
import se.inera.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.webcert.service.mail.MailNotificationServiceImpl;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MailNotificationServiceMockedTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private HSAWebServiceCalls hsaClient;

    @InjectMocks
    private MailNotificationServiceImpl mailNotificationService;

    @Test
    public void sendMailForIncomingQuestionWithTimeoutThrowsNoException() throws Exception {
        doThrow(new MailSendException("Timeout")).when(mailSender).send(any(MimeMessage.class));
        GetHsaUnitResponseType getHsaUnitResponseType = new GetHsaUnitResponseType();
        getHsaUnitResponseType.setEmail("test@test.invalid");
        when(hsaClient.callGetHsaunit(anyString())).thenReturn(getHsaUnitResponseType);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        mailNotificationService.sendMailForIncomingQuestion(fragaSvar("enhetsid"));
    }

    @Test
    public void sendMailForIncomingAnswerWithTimeoutThrowsNoException() throws Exception {
        doThrow(new MailSendException("Timeout")).when(mailSender).send(any(MimeMessage.class));
        GetHsaUnitResponseType getHsaUnitResponseType = new GetHsaUnitResponseType();
        getHsaUnitResponseType.setEmail("test@test.invalid");
        when(hsaClient.callGetHsaunit(anyString())).thenReturn(getHsaUnitResponseType);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session)null));
        mailNotificationService.sendMailForIncomingAnswer(fragaSvar("enhetsid"));
    }

    @Test
    public void setAdminMailAddress() throws Exception {
    }

    private FragaSvar fragaSvar(String enhetsId) {
        FragaSvar fragaSvar = new FragaSvar();
        fragaSvar.setVardperson(new Vardperson());
        fragaSvar.getVardperson().setEnhetsId(enhetsId);
        fragaSvar.setInternReferens(1L);
        fragaSvar.setIntygsReferens(new IntygsReferens());
        fragaSvar.getIntygsReferens().setIntygsId("1L");
        fragaSvar.getIntygsReferens().setIntygsTyp("FK7263");
        return fragaSvar;
    }

}