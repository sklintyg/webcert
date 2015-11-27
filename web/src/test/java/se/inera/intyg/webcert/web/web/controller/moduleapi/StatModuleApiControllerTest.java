package se.inera.intyg.webcert.web.web.controller.moduleapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.common.common.security.authority.UserPrivilege;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;
import se.inera.webcert.hsa.model.Mottagning;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
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

@RunWith(MockitoJUnitRunner.class)
public class StatModuleApiControllerTest {

    private static final int OK = 200;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private FragaSvarService fragaSvarService;

    @Mock
    private UtkastService intygDraftService;

    @Captor
    private ArgumentCaptor<List<String>> listCaptor;

    @InjectMocks
    private StatModuleApiController statController;

    private WebCertUser mockUser;

    private Map<String, Long> fragaSvarStatsMap;

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

        intygStatsMap = new HashMap<>();

        intygStatsMap.put("VE1M1", 1L);
        intygStatsMap.put("VE1M2", 2L);
        intygStatsMap.put("VE2", 2L);

        mockUser = new WebCertUser();

        mockUser.setRoles(getGrantedRole());
        mockUser.setAuthorities(getGrantedPrivileges());

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

        when(webCertUserService.getUser()).thenReturn(mockUser);
    }

    @Test
    public void testGetStatisticsWithSelectedUnitVE2() {

        mockUser.setValdVardenhet(ve2);

        when(fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(anyListOf(String.class))).thenReturn(fragaSvarStatsMap);
        when(intygDraftService.getNbrOfUnsignedDraftsByCareUnits(anyListOf(String.class))).thenReturn(intygStatsMap);

        Response response = statController.getStatistics();

        assertNotNull(response);
        assertEquals(OK, response.getStatus());

        StatsResponse statsResponse = (StatsResponse) response.getEntity();
        assertNotNull(statsResponse);

        assertEquals(2, statsResponse.getTotalNbrOfUnhandledFragaSvarOnSelected());
        assertEquals(9, statsResponse.getTotalNbrOfUnhandledFragaSvarOnOtherThanSelected());

        assertEquals(2, statsResponse.getTotalNbrOfUnsignedDraftsOnSelected());
        assertEquals(3, statsResponse.getTotalNbrOfUnsignedDraftsOnOtherThanSelected());
    }

    @Test
    public void testGetStatisticsWithSelectedUnitVE3() {

        mockUser.setValdVardenhet(ve3);

        when(fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(anyListOf(String.class))).thenReturn(fragaSvarStatsMap);
        when(intygDraftService.getNbrOfUnsignedDraftsByCareUnits(anyListOf(String.class))).thenReturn(intygStatsMap);

        Response response = statController.getStatistics();

        assertNotNull(response);
        assertEquals(OK, response.getStatus());

        StatsResponse statsResponse = (StatsResponse) response.getEntity();
        assertNotNull(statsResponse);

        assertEquals(1, statsResponse.getTotalNbrOfUnhandledFragaSvarOnSelected());
        assertEquals(10, statsResponse.getTotalNbrOfUnhandledFragaSvarOnOtherThanSelected());

        assertEquals(0, statsResponse.getTotalNbrOfUnsignedDraftsOnSelected());
        assertEquals(5, statsResponse.getTotalNbrOfUnsignedDraftsOnOtherThanSelected());
    }

    @Test
    public void testGetStatisticsWithSelectedUnitVE4() {

        mockUser.setValdVardenhet(ve4);

        when(fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(anyListOf(String.class))).thenReturn(fragaSvarStatsMap);
        when(intygDraftService.getNbrOfUnsignedDraftsByCareUnits(anyListOf(String.class))).thenReturn(intygStatsMap);

        Response response = statController.getStatistics();

        verify(webCertUserService).getUser();

        verify(fragaSvarService).getNbrOfUnhandledFragaSvarForCareUnits(listCaptor.capture());

        List<String> listArgs = listCaptor.getValue();
        assertEquals(7, listArgs.size());

        assertNotNull(response);
        assertEquals(OK, response.getStatus());

        StatsResponse statsResponse = (StatsResponse) response.getEntity();
        assertNotNull(statsResponse);

        assertEquals(0, statsResponse.getTotalNbrOfUnhandledFragaSvarOnSelected());
        assertEquals(11, statsResponse.getTotalNbrOfUnhandledFragaSvarOnOtherThanSelected());
    }

    @Test
    public void testGetStatisticsWithSelectedUnitVE1() {

        mockUser.setValdVardenhet(ve1);

        when(fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(anyListOf(String.class))).thenReturn(fragaSvarStatsMap);
        when(intygDraftService.getNbrOfUnsignedDraftsByCareUnits(anyListOf(String.class))).thenReturn(intygStatsMap);

        Response response = statController.getStatistics();

        verify(webCertUserService).getUser();

        verify(fragaSvarService).getNbrOfUnhandledFragaSvarForCareUnits(listCaptor.capture());

        List<String> listArgs = listCaptor.getValue();
        assertEquals(7, listArgs.size());

        assertNotNull(response);
        assertEquals(OK, response.getStatus());

        StatsResponse statsResponse = (StatsResponse) response.getEntity();
        assertNotNull(statsResponse);

        assertEquals(8, statsResponse.getTotalNbrOfUnhandledFragaSvarOnSelected());
        assertEquals(3, statsResponse.getTotalNbrOfUnhandledFragaSvarOnOtherThanSelected());

        assertEquals(0, statsResponse.getTotalNbrOfUnsignedDraftsOnSelected());
        assertEquals(2, statsResponse.getTotalNbrOfUnsignedDraftsOnOtherThanSelected());

        StatsResponse refStatsResponse = getReference("StatModuleApiControllerTest/reference.json");
        assertEquals(refStatsResponse.toString(), statsResponse.toString());
    }

    @Test
    public void testWebcertUserIsNull() {
        when(webCertUserService.getUser()).thenReturn(null);

        when(fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(anyListOf(String.class))).thenReturn(fragaSvarStatsMap);
        when(intygDraftService.getNbrOfUnsignedDraftsByCareUnits(anyListOf(String.class))).thenReturn(intygStatsMap);

        Response response = statController.getStatistics();
        assertNotNull(response);

        verify(webCertUserService).getUser();
        assertEquals(OK, response.getStatus());
    }

    private StatsResponse getReference(String referenceFilePath) {
        try {
            return new CustomObjectMapper().readValue(new ClassPathResource(
                    referenceFilePath).getFile(), StatsResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
