package se.inera.webcert.service;

import javax.jms.Session;
import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;

import static org.joda.time.LocalDateTime.now;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.log.messages.IntygReadMessage;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.web.service.WebCertUserService;
import se.inera.webcert.web.service.WebCertUserServiceImpl;

/**
 * Created by pehr on 13/11/13.
 */
@RunWith(MockitoJUnitRunner.class)
public class LogServiceImplTest {

    @Mock
    private JmsTemplate template = mock(JmsTemplate.class);

    @Mock
    protected WebCertUserService webCertUserService = new WebCertUserServiceImpl();

    @InjectMocks
    LogServiceImpl logService = new LogServiceImpl();

    @Test
    public void serviceSendsDocumentAndIdForCreate() throws Exception {
        logService.systemId = "webcert";

        ArgumentCaptor<MessageCreator> messageCreatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);

        when(webCertUserService.getWebCertUser()).thenReturn(createWcUser());

        GetCertificateForCareResponseType certificate = certificate();

        logService.logReadOfIntyg(certificate);

        verify(template, only()).send(messageCreatorCaptor.capture());

        MessageCreator messageCreator = messageCreatorCaptor.getValue();

        Session session = mock(Session.class);
        ArgumentCaptor<IntygReadMessage> intygReadMessageCaptor = ArgumentCaptor.forClass(IntygReadMessage.class);
        when(session.createObjectMessage(intygReadMessageCaptor.capture())).thenReturn(null);

        messageCreator.createMessage(session);

        IntygReadMessage intygReadMessage = intygReadMessageCaptor.getValue();

        assertNotNull(intygReadMessage.getLogId());
        assertEquals("Läsa", intygReadMessage.getActivityType());
        assertEquals("Vård och behandling", intygReadMessage.getPurpose());
        assertEquals("Intyg", intygReadMessage.getResourceType());

        assertEquals("HSAID", intygReadMessage.getUserId());

        assertEquals("ENHETS ID", intygReadMessage.getEnhet().getEnhetsId());
        assertEquals("Enhet", intygReadMessage.getEnhet().getEnhetsNamn());
        assertEquals("VARDGIVARE ID", intygReadMessage.getEnhet().getVardgivareId());
        assertEquals("Vårdgivare", intygReadMessage.getEnhet().getVardgivareNamn());

        assertEquals("19121212-1212", intygReadMessage.getPatient().getPatientId());
        assertEquals("Hans Olof van der Test", intygReadMessage.getPatient().getPatientNamn());

        assertTrue(intygReadMessage.getTimestamp().minusSeconds(10).isBefore(now()));
        assertTrue(intygReadMessage.getTimestamp().plusSeconds(10).isAfter(now()));

        assertEquals("webcert", intygReadMessage.getSystemId());
    }

    private GetCertificateForCareResponseType certificate() throws Exception {
        return JAXBContext.newInstance(GetCertificateForCareResponseType.class).createUnmarshaller().unmarshal(
                new StreamSource(new ClassPathResource("LogServiceTest/certificate.xml").getInputStream()),
                GetCertificateForCareResponseType.class).getValue();
    }

    private WebCertUser createWcUser() {
        WebCertUser wcu = new WebCertUser();
        wcu.setHsaId("HSAID");
        return wcu;
    }
}
