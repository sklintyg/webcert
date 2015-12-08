package se.inera.intyg.webcert.web.service.mail;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import se.inera.intyg.webcert.integration.hsa.client.OrganizationUnitService;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.riv.infrastructure.directory.organization.getunitresponder.v1.GetUnitResponseType;
import se.riv.infrastructure.directory.organization.getunitresponder.v1.UnitType;

@RunWith(MockitoJUnitRunner.class)
public class MailNotificationServiceMockedTest {

    @Mock
    private JavaMailSender mailSender;

//    @Mock
//    private HSAWebServiceCalls hsaClient;
    @Mock
    private OrganizationUnitService organizationUnitService;

    @Mock
    private MonitoringLogService monitoringService;

    @Mock
    private UtkastRepository utkastRepository;

    @InjectMocks
    private MailNotificationServiceImpl mailNotificationService;

    @Before
    public void setUp() {
        mailNotificationService.setFromAddress("no-reply@webcert.intygstjanster.se");
    }

    @Test
    public void sendMailForIncomingQuestionWithTimeoutThrowsNoException() throws Exception {
        doThrow(new MailSendException("Timeout")).when(mailSender).send(any(MimeMessage.class));
        mockOrganizationUnitServiceGetUnit();
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        mailNotificationService.sendMailForIncomingQuestion(fragaSvar("enhetsid"));
    }

    @Test
    public void sendMailForIncomingAnswerWithTimeoutThrowsNoException() throws Exception {
        doThrow(new MailSendException("Timeout")).when(mailSender).send(any(MimeMessage.class));
        mockOrganizationUnitServiceGetUnit();
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        mailNotificationService.sendMailForIncomingAnswer(fragaSvar("enhetsid"));
    }

    private void mockOrganizationUnitServiceGetUnit() {
        GetUnitResponseType getHsaUnitResponseType = new GetUnitResponseType();
        UnitType unit = new UnitType();
        unit.setMail("test@test.invalid");
        unit.setUnitHsaId("enhetsid");
        getHsaUnitResponseType.setUnit(unit);
        when(organizationUnitService.getUnit(anyString())).thenReturn(getHsaUnitResponseType);
    }

    @Test
    public void testNoHSAResponse() {
        try {
            SOAPFault soapFault = SOAPFactory.newInstance().createFault();
            soapFault.setFaultString("Connection reset");
            when(organizationUnitService.getUnit(anyString())).thenThrow(new SOAPFaultException(soapFault));
        } catch (SOAPException e) {
            e.printStackTrace();
        }
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
