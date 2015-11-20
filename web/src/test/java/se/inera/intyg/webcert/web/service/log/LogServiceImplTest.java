package se.inera.intyg.webcert.web.service.log;

import static org.joda.time.LocalDateTime.now;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.certificate.modules.support.api.dto.Personnummer;
import se.inera.log.messages.ActivityPurpose;
import se.inera.log.messages.ActivityType;
import se.inera.log.messages.IntygReadMessage;
import se.inera.webcert.common.security.authority.UserPrivilege;
import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.jms.Session;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pehr on 13/11/13.
 */
@RunWith(MockitoJUnitRunner.class)
public class LogServiceImplTest {

    private static final int DELAY = 10;

    @Mock
    private JmsTemplate template = mock(JmsTemplate.class);

    @Mock
    private WebCertUserService userService = mock(WebCertUserService.class);

    @InjectMocks
    private LogServiceImpl logService = new LogServiceImpl();

    @Test
    public void serviceSendsDocumentAndIdForCreate() throws Exception {
        ReflectionTestUtils.setField(logService, "systemId", "webcert");
        ReflectionTestUtils.setField(logService, "systemName", "WebCert");

        when(userService.getUser()).thenReturn(createUser());

        ArgumentCaptor<MessageCreator> messageCreatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);

        LogRequest logRequest = new LogRequest();
        logRequest.setIntygId("abc123");
        logRequest.setPatientId(new Personnummer("19121212-1212"));
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

        assertEquals("19121212-1212", intygReadMessage.getPatient().getPatientId().getPersonnummer());
        assertEquals("Hans Olof van der Test", intygReadMessage.getPatient().getPatientNamn());

        assertTrue(intygReadMessage.getTimestamp().minusSeconds(DELAY).isBefore(now()));
        assertTrue(intygReadMessage.getTimestamp().plusSeconds(DELAY).isAfter(now()));

        assertEquals("webcert", intygReadMessage.getSystemId());
        assertEquals("WebCert", intygReadMessage.getSystemName());
    }

    private WebCertUser createUser() {

        Vardenhet ve = new Vardenhet("VARDENHET_ID", "Vårdenheten");

        Vardgivare vg = new Vardgivare("VARDGIVARE_ID", "Vårdgivaren");
        vg.setVardenheter(Collections.singletonList(ve));

        WebCertUser user = new WebCertUser();
        user.setRoles(getGrantedRole());
        user.setAuthorities(getGrantedPrivileges());
        user.setHsaId("HSAID");
        user.setNamn("Markus Gran");
        user.setVardgivare(Collections.singletonList(vg));
        user.changeValdVardenhet("VARDENHET_ID");

        return user;
    }

    private Map<String, UserRole> getGrantedRole() {
        Map<String, UserRole> map = new HashMap<>();
        map.put(UserRole.ROLE_LAKARE.name(), UserRole.ROLE_LAKARE);
        return map;
    }

    private Map<String, UserPrivilege> getGrantedPrivileges() {
        List<UserPrivilege> list = Arrays.asList(UserPrivilege.values());

        // convert list to map
        Map<String, UserPrivilege> privilegeMap = Maps.uniqueIndex(list, new Function<UserPrivilege, String>() {
            @Override
            public String apply(UserPrivilege userPrivilege) {
                return userPrivilege.name();
            }
        });

        return privilegeMap;
    }

}
