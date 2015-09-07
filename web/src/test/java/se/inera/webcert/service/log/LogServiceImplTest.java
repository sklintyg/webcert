package se.inera.webcert.service.log;

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
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.log.messages.ActivityPurpose;
import se.inera.log.messages.ActivityType;
import se.inera.log.messages.IntygReadMessage;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.service.user.WebCertUserService;
import se.inera.webcert.service.user.dto.WebCertUser;

import javax.jms.Session;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by pehr on 13/11/13.
 */
@RunWith(MockitoJUnitRunner.class)
public class LogServiceImplTest {

    @Mock
    private JmsTemplate template = mock(JmsTemplate.class);

    @Mock
    private WebCertUserService userService = mock(WebCertUserService.class);

    @InjectMocks
    LogServiceImpl logService = new LogServiceImpl();

    @Test
    public void serviceSendsDocumentAndIdForCreate() throws Exception {
        ReflectionTestUtils.setField(logService, "systemId", "webcert");
        ReflectionTestUtils.setField(logService, "systemName", "WebCert");
        
        Mockito.when(userService.getWebCertUser()).thenReturn(createWcUser());

        ArgumentCaptor<MessageCreator> messageCreatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);

        LogRequest logRequest = new LogRequest();
        logRequest.setIntygId("abc123");
        logRequest.setPatientId("19121212-1212");
        logRequest.setPatientName("Hans Olof van der Test");
        
        logService.logReadIntyg(logRequest);

        verify(template, only()).send(messageCreatorCaptor.capture());

        MessageCreator messageCreator = messageCreatorCaptor.getValue();

        Session session = mock(Session.class);
        ArgumentCaptor<IntygReadMessage> intygReadMessageCaptor = ArgumentCaptor.forClass(IntygReadMessage.class);
        when(session.createObjectMessage(intygReadMessageCaptor.capture())).thenReturn(null);

        messageCreator.createMessage(session);

        IntygReadMessage intygReadMessage = intygReadMessageCaptor.getValue();

        assertNotNull(intygReadMessage.getLogId());
        assertEquals(ActivityType.READ, intygReadMessage.getActivityType());
        assertEquals(ActivityPurpose.CARE_TREATMENT, intygReadMessage.getPurpose());
        assertEquals("Intyg", intygReadMessage.getResourceType());
        assertEquals("abc123", intygReadMessage.getActivityLevel());

        assertEquals("HSAID", intygReadMessage.getUserId());
        assertEquals("Markus Gran", intygReadMessage.getUserName());

        assertEquals("VARDENHET_ID", intygReadMessage.getUserCareUnit().getEnhetsId());
        assertEquals("Vårdenheten", intygReadMessage.getUserCareUnit().getEnhetsNamn());
        assertEquals("VARDGIVARE_ID", intygReadMessage.getUserCareUnit().getVardgivareId());
        assertEquals("Vårdgivaren", intygReadMessage.getUserCareUnit().getVardgivareNamn());

        assertEquals("19121212-1212", intygReadMessage.getPatient().getPatientId());
        assertEquals("Hans Olof van der Test", intygReadMessage.getPatient().getPatientNamn());

        assertTrue(intygReadMessage.getTimestamp().minusSeconds(10).isBefore(now()));
        assertTrue(intygReadMessage.getTimestamp().plusSeconds(10).isAfter(now()));

        assertEquals("webcert", intygReadMessage.getSystemId());
        assertEquals("WebCert", intygReadMessage.getSystemName());
    }

    private WebCertUser createWcUser() {
        
        Vardenhet ve = new Vardenhet("VARDENHET_ID", "Vårdenheten");
        
        Vardgivare vg = new Vardgivare("VARDGIVARE_ID", "Vårdgivaren");
        vg.setVardenheter(Arrays.asList(ve));
        
        WebCertUser wcu = new WebCertUser(new ArrayList<GrantedAuthority>());
        wcu.setHsaId("HSAID");
        wcu.setNamn("Markus Gran");
        wcu.setVardgivare(Arrays.asList(vg));
        wcu.changeValdVardenhet("VARDENHET_ID");
        
        return wcu;
    }
}
