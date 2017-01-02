/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.moduleapi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.intyg.infra.integration.hsa.model.Mottagning;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserDetails;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.StatsResponse;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StatModuleApiControllerTest extends AuthoritiesConfigurationTestSetup {

    private static final int OK = 200;

    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private AuthoritiesHelper authoritiesHelper;
    @Mock
    private FragaSvarService fragaSvarService;
    @Mock
    private ArendeService arendeService;
    @Mock
    private UtkastService intygDraftService;
    @Captor
    private ArgumentCaptor<List<String>> listCaptor;
    @Captor
    private ArgumentCaptor<Set<String>> intygsTypSetCaptor;
    @InjectMocks
    private StatModuleApiController statController;

    private WebCertUser mockUser;
    private Map<String, Long> fragaSvarStatsMap;
    private Map<String, Long> arendeStatsMap;
    private Map<String, Long> intygStatsMap;
    private Vardenhet ve1, ve2, ve3, ve4;


    @Before
    public void setupDataAndExpectations() {

        fragaSvarStatsMap = new HashMap<>();

        fragaSvarStatsMap.put("VE1", 2L);
        fragaSvarStatsMap.put("VE1M1", 3L);
        fragaSvarStatsMap.put("VE1M2", 3L);
        fragaSvarStatsMap.put("VE2", 2L);
        fragaSvarStatsMap.put("VE3", 1L);

        arendeStatsMap = new HashMap<>();

        arendeStatsMap.put("VE1", 2L);
        arendeStatsMap.put("VE1M1", 3L);
        arendeStatsMap.put("VE1M2", 3L);
        arendeStatsMap.put("VE2", 2L);
        arendeStatsMap.put("VE3", 1L);

        intygStatsMap = new HashMap<>();

        intygStatsMap.put("VE1M1", 1L);
        intygStatsMap.put("VE1M2", 2L);
        intygStatsMap.put("VE2", 2L);

        mockUser = new WebCertUser();

        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);
        mockUser.setRoles(AuthoritiesResolverUtil.toMap(role));
        mockUser.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));

        ve1 = new Vardenhet("VE1", "Vardenhet1");
        ve1.getMottagningar().add(new Mottagning("VE1M1", "Mottagning1"));
        ve1.getMottagningar().add(new Mottagning("VE1M2", "Mottagning2"));

        ve2 = new Vardenhet("VE2", "Vardenhet2");
        ve2.getMottagningar().add(new Mottagning("VE2M1", "Mottagning3"));

        ve3 = new Vardenhet("VE3", "Vardenhet3");

        ve4 = new Vardenhet("VE4", "Vardenhet4");

        Vardgivare vg = new Vardgivare("VG1", "Vardgivaren");
        vg.setVardenheter(Arrays.asList(ve1, ve2, ve3, ve4));

        mockUser.setVardgivare(Collections.singletonList(vg));
        mockUser.setValdVardgivare(vg);

        when(authoritiesHelper.getIntygstyperForPrivilege(any(UserDetails.class), anyString())).thenReturn(Stream.of("fk7263").collect(Collectors.toSet()));
        when(webCertUserService.getUser()).thenReturn(mockUser);
    }

    @Test
    public void testGetStatisticsWithSelectedUnitVE2() {

        mockUser.setValdVardenhet(ve2);

        when(fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(anyListOf(String.class), anySetOf(String.class))).thenReturn(fragaSvarStatsMap);
        when(arendeService.getNbrOfUnhandledArendenForCareUnits(anyListOf(String.class), anySetOf(String.class))).thenReturn(arendeStatsMap);
        when(intygDraftService.getNbrOfUnsignedDraftsByCareUnits(anyListOf(String.class))).thenReturn(intygStatsMap);

        Response response = statController.getStatistics();

        assertNotNull(response);
        assertEquals(OK, response.getStatus());

        StatsResponse statsResponse = (StatsResponse) response.getEntity();
        assertNotNull(statsResponse);

        assertEquals(4, statsResponse.getTotalNbrOfUnhandledFragaSvarOnSelected());
        assertEquals(18, statsResponse.getTotalNbrOfUnhandledFragaSvarOnOtherThanSelected());

        assertEquals(2, statsResponse.getTotalNbrOfUnsignedDraftsOnSelected());
        assertEquals(3, statsResponse.getTotalNbrOfUnsignedDraftsOnOtherThanSelected());
    }

    @Test
    public void testGetStatisticsWithSelectedUnitVE3() {

        mockUser.setValdVardenhet(ve3);

        when(fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(anyListOf(String.class), anySetOf(String.class))).thenReturn(fragaSvarStatsMap);
        when(arendeService.getNbrOfUnhandledArendenForCareUnits(anyListOf(String.class), anySetOf(String.class))).thenReturn(arendeStatsMap);
        when(intygDraftService.getNbrOfUnsignedDraftsByCareUnits(anyListOf(String.class))).thenReturn(intygStatsMap);

        Response response = statController.getStatistics();

        assertNotNull(response);
        assertEquals(OK, response.getStatus());

        StatsResponse statsResponse = (StatsResponse) response.getEntity();
        assertNotNull(statsResponse);

        assertEquals(2, statsResponse.getTotalNbrOfUnhandledFragaSvarOnSelected());
        assertEquals(20, statsResponse.getTotalNbrOfUnhandledFragaSvarOnOtherThanSelected());

        assertEquals(0, statsResponse.getTotalNbrOfUnsignedDraftsOnSelected());
        assertEquals(5, statsResponse.getTotalNbrOfUnsignedDraftsOnOtherThanSelected());
    }

    @Test
    public void testGetStatisticsWithSelectedUnitVE4() {

        mockUser.setValdVardenhet(ve4);

        when(fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(anyListOf(String.class), anySetOf(String.class))).thenReturn(fragaSvarStatsMap);
        when(arendeService.getNbrOfUnhandledArendenForCareUnits(anyListOf(String.class), anySetOf(String.class))).thenReturn(arendeStatsMap);
        when(intygDraftService.getNbrOfUnsignedDraftsByCareUnits(anyListOf(String.class))).thenReturn(intygStatsMap);

        Response response = statController.getStatistics();

        verify(webCertUserService).getUser();

        verify(fragaSvarService).getNbrOfUnhandledFragaSvarForCareUnits(listCaptor.capture(), intygsTypSetCaptor.capture());

        List<String> listArgs = listCaptor.getValue();
        assertEquals(7, listArgs.size());

        assertNotNull(response);
        assertEquals(OK, response.getStatus());

        StatsResponse statsResponse = (StatsResponse) response.getEntity();
        assertNotNull(statsResponse);

        assertEquals(0, statsResponse.getTotalNbrOfUnhandledFragaSvarOnSelected());
        assertEquals(22, statsResponse.getTotalNbrOfUnhandledFragaSvarOnOtherThanSelected());
    }

    @Test
    public void testGetStatisticsWithSelectedUnitVE1() {

        mockUser.setValdVardenhet(ve1);

        when(fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(anyListOf(String.class), anySetOf(String.class))).thenReturn(fragaSvarStatsMap);
        when(arendeService.getNbrOfUnhandledArendenForCareUnits(anyListOf(String.class), anySetOf(String.class))).thenReturn(arendeStatsMap);
        when(intygDraftService.getNbrOfUnsignedDraftsByCareUnits(anyListOf(String.class))).thenReturn(intygStatsMap);

        Response response = statController.getStatistics();

        verify(webCertUserService).getUser();

        verify(fragaSvarService).getNbrOfUnhandledFragaSvarForCareUnits(listCaptor.capture(), intygsTypSetCaptor.capture());

        List<String> listArgs = listCaptor.getValue();
        assertEquals(7, listArgs.size());

        assertNotNull(response);
        assertEquals(OK, response.getStatus());

        StatsResponse statsResponse = (StatsResponse) response.getEntity();
        assertNotNull(statsResponse);

        assertEquals(16, statsResponse.getTotalNbrOfUnhandledFragaSvarOnSelected());
        assertEquals(6, statsResponse.getTotalNbrOfUnhandledFragaSvarOnOtherThanSelected());

        assertEquals(0, statsResponse.getTotalNbrOfUnsignedDraftsOnSelected());
        assertEquals(2, statsResponse.getTotalNbrOfUnsignedDraftsOnOtherThanSelected());

        StatsResponse refStatsResponse = getReference("StatModuleApiControllerTest/reference.json");
        assertEquals(refStatsResponse.toString(), statsResponse.toString());
    }

    @Test
    public void testWebcertUserIsNull() {
        when(webCertUserService.getUser()).thenReturn(null);

        when(fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(anyListOf(String.class), anySetOf(String.class))).thenReturn(fragaSvarStatsMap);
        when(arendeService.getNbrOfUnhandledArendenForCareUnits(anyListOf(String.class), anySetOf(String.class))).thenReturn(arendeStatsMap);
        when(intygDraftService.getNbrOfUnsignedDraftsByCareUnits(anyListOf(String.class))).thenReturn(intygStatsMap);

        Response response = statController.getStatistics();
        assertNotNull(response);

        verify(webCertUserService).getUser();
        assertEquals(OK, response.getStatus());
    }

    @Test
    public void testMergeMaps() {
        Map<String, Long> m1 = new HashMap<>();
        m1.put("enhet-1", 2l);
        m1.put("enhet-2", 3l);
        Map<String, Long> m2 = new HashMap<>();
        m2.put("enhet-2", 2l);
        m2.put("enhet-3", 4l);
        Map<String, Long> m3 = statController.mergeArendeAndFragaSvarMaps(m1, m2);
        assertEquals(3, m3.size());
        assertEquals((long) 2, (long) m3.get("enhet-1"));
        assertEquals((long) 5, (long) m3.get("enhet-2"));
        assertEquals((long) 4, (long) m3.get("enhet-3"));
    }

    private StatsResponse getReference(String referenceFilePath) {
        try {
            return new CustomObjectMapper().readValue(new ClassPathResource(
                    referenceFilePath).getFile(), StatsResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
