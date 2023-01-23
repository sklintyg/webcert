/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.destination.DestinationResolutionException;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.logmessages.ActivityPurpose;
import se.inera.intyg.infra.logmessages.ActivityType;
import se.inera.intyg.infra.logmessages.PdlLogMessage;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.jms.Session;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by pehr on 13/11/13.
 */
@RunWith(MockitoJUnitRunner.class)
public class LogServiceImplTest extends AuthoritiesConfigurationTestSetup {

    private static final int DELAY = 10;

    @Mock
    private JmsTemplate template = mock(JmsTemplate.class);

    @Mock
    private WebCertUserService userService = mock(WebCertUserService.class);

    @InjectMocks
    private LogServiceImpl logService = new LogServiceImpl();

    private ObjectMapper objectMapper = new CustomObjectMapper();

    @Before
    public void setup() {
        LogMessagePopulator logMessagePopulator = new LogMessagePopulatorImpl();
        ReflectionTestUtils.setField(logMessagePopulator, "systemId", "webcert");
        ReflectionTestUtils.setField(logMessagePopulator, "systemName", "WebCert");
        logService.setLogMessagePopulator(logMessagePopulator);
    }

    @Test
    public void serviceSendsDocumentAndIdForCreate() throws Exception {
        when(userService.getUser()).thenReturn(createUser());

        ArgumentCaptor<MessageCreator> messageCreatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);

        LogRequest logRequest = new LogRequest();
        logRequest.setIntygId("abc123");
        logRequest.setPatientId(createPnr("19121212-1212"));
        logRequest.setPatientName("Hans Olof van der Test");
        logRequest.setTestIntyg(false);

        logService.logReadIntyg(logRequest);

        verify(template, only()).send(messageCreatorCaptor.capture());

        MessageCreator messageCreator = messageCreatorCaptor.getValue();

        Session session = mock(Session.class);
        ArgumentCaptor<String> intygReadMessageCaptor = ArgumentCaptor.forClass(String.class);
        when(session.createTextMessage(intygReadMessageCaptor.capture())).thenReturn(null);

        messageCreator.createMessage(session);

        String body = intygReadMessageCaptor.getValue();

        PdlLogMessage intygReadMessage = objectMapper.readValue(body, PdlLogMessage.class);
        assertNotNull(intygReadMessage.getLogId());
        assertEquals(ActivityType.READ, intygReadMessage.getActivityType());
        assertEquals(ActivityPurpose.CARE_TREATMENT, intygReadMessage.getPurpose());
        assertEquals("Intyg", intygReadMessage.getPdlResourceList().get(0).getResourceType());
        assertEquals("abc123", intygReadMessage.getActivityLevel());

        assertEquals("HSAID", intygReadMessage.getUserId());
        assertEquals("", intygReadMessage.getUserName());
        assertEquals("Läkare på vårdcentralen", intygReadMessage.getUserAssignment());
        assertEquals("Överläkare", intygReadMessage.getUserTitle());

        assertEquals("VARDENHET_ID", intygReadMessage.getUserCareUnit().getEnhetsId());
        assertEquals("Vårdenheten", intygReadMessage.getUserCareUnit().getEnhetsNamn());
        assertEquals("VARDGIVARE_ID", intygReadMessage.getUserCareUnit().getVardgivareId());
        assertEquals("Vårdgivaren", intygReadMessage.getUserCareUnit().getVardgivareNamn());

        assertEquals("191212121212", intygReadMessage.getPdlResourceList().get(0).getPatient().getPatientId());
        assertEquals("", intygReadMessage.getPdlResourceList().get(0).getPatient().getPatientNamn());

        assertTrue(intygReadMessage.getTimestamp().minusSeconds(DELAY).isBefore(now()));
        assertTrue(intygReadMessage.getTimestamp().plusSeconds(DELAY).isAfter(now()));

        assertEquals("webcert", intygReadMessage.getSystemId());
        assertEquals("WebCert", intygReadMessage.getSystemName());
    }

    @Test(expected = JmsException.class)
    public void logServiceJmsException() throws Exception {
        when(userService.getUser()).thenReturn(createUser());
        doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));

        LogRequest logRequest = new LogRequest();
        logRequest.setIntygId("abc123");
        logRequest.setPatientId(createPnr("19121212-1212"));
        logRequest.setPatientName("Hans Olof van der Test");
        logRequest.setTestIntyg(false);

        try {
            logService.logReadIntyg(logRequest);
        } finally {
            verify(template, times(1)).send(any(MessageCreator.class));
        }
    }

    public void testActivityArgsAreIdenticalToAdditionalInfo() {
        LogRequest logRequest = new LogRequest();
        logRequest.setIntygId("abc123");
        logRequest.setPatientId(createPnr("19121212-1212"));
        logRequest.setPatientName("Hans Olof van der Test");
        logRequest.setAdditionalInfo("this is additional");

        logService.logPrintIntygAsPDF(logRequest);
    }

    @Test
    public void logServiceTestIntygShouldNotBeLogged() throws Exception {
        when(userService.getUser()).thenReturn(createUser());

        LogRequest logRequest = new LogRequest();
        logRequest.setIntygId("abc123");
        logRequest.setPatientId(createPnr("19121212-1212"));
        logRequest.setPatientName("Hans Olof van der Test");
        logRequest.setTestIntyg(true);

        logService.logReadIntyg(logRequest);

        verify(template, times(0)).send(any(MessageCreator.class));
    }

    private WebCertUser createUser() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        Vardenhet ve = new Vardenhet("VARDENHET_ID", "Vårdenheten");

        Vardgivare vg = new Vardgivare("VARDGIVARE_ID", "Vårdgivaren");
        vg.setVardenheter(Collections.singletonList(ve));

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        user.setHsaId("HSAID");
        user.setNamn("Markus Gran");
        user.setVardgivare(Collections.singletonList(vg));
        user.changeValdVardenhet("VARDENHET_ID");
        user.setTitel("Överläkare");
        user.setMiuNamnPerEnhetsId(buildMiUMap());

        return user;
    }

    private Map<String, String> buildMiUMap() {
        Map<String, String> map = new HashMap<>();
        map.put("VARDENHET_ID", "Läkare på vårdcentralen");
        return map;
    }

    private Personnummer createPnr(String personId) {
        return Personnummer.createPersonnummer(personId)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));
    }

}
