package se.inera.webcert.web.controller.moduleapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.webcert.hsa.model.Mottagning;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.service.FragaSvarService;
import se.inera.webcert.web.controller.moduleapi.dto.StatsResponse;
import se.inera.webcert.web.service.WebCertUserService;

@RunWith(MockitoJUnitRunner.class)
public class StatModuleApiControllerTest {
    
    private static final int OK = 200;
    
    @Mock
    private WebCertUserService webCertUserService;
    
    @Mock
    private FragaSvarService fragaSvarService;
    
    @InjectMocks 
    private StatModuleApiController statController;
    
    
    @Test
    public void testGetStatistics() {
        
        List<String> ids = Arrays.asList("VE1","VE2");
        
        List<Vardgivare> vardgivare = createVardgivare();
        
        WebCertUser mockUser = Mockito.mock(WebCertUser.class);
        
        Mockito.when(webCertUserService.getWebCertUser()).thenReturn(mockUser);
        
        Mockito.when(mockUser.getIdsOfAllVardenheter()).thenReturn(ids);
        Mockito.when(mockUser.getVardgivare()).thenReturn(vardgivare);
        Mockito.when(mockUser.getValdVardenhet()).thenReturn(new Vardenhet("VE1", "Valdvardenhet"));
        
        Map<String, Long> statsMap = createStatsMap();
        Mockito.when(fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(ids)).thenReturn(statsMap);
                
        Response response = statController.getStatistics();
        
        Mockito.verify(webCertUserService).getWebCertUser();
        Mockito.verify(fragaSvarService).getNbrOfUnhandledFragaSvarForCareUnits(ids);
                
        assertNotNull(response);
        assertEquals(OK, response.getStatus());
        
        StatsResponse statsResponse = (StatsResponse) response.getEntity();
        assertNotNull(statsResponse);
        
        StatsResponse refStatsResponse = getReference("StatModuleApiControllerTest/reference.json");
        assertEquals(refStatsResponse.toString(), statsResponse.toString());
        
        // VE1 is selected, that gives -2 from total of 16
        assertEquals("Should be 14 since VE1 is selected", 14, statsResponse.getTotalNbrOfUnhandledFragaSvarOnOtherThanSelected());
        assertEquals("Should be 2 since VE1 is selected", 2, statsResponse.getTotalNbrOfUnhandledFragaSvarOnSelected());
    }
        
    private StatsResponse getReference(String referenceFilePath) {
        try {
            return new CustomObjectMapper().readValue(new ClassPathResource(
                    referenceFilePath).getFile(), StatsResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
        
    private Map<String, Long> createStatsMap() {
        
        Map<String, Long> statsMap = new HashMap<String, Long>(); 
        
        statsMap.put("VE1", new Long(2L));
        statsMap.put("VE1M1", new Long(3L));
        statsMap.put("VE1M2", new Long(3L));
        statsMap.put("VE2", new Long(2L));
        statsMap.put("VE2M1", new Long(2L));
        statsMap.put("VE2M2", new Long(4L));
        
        return statsMap;
    }
    
    private List<Vardgivare> createVardgivare() {
        
        Vardgivare vg1 = new Vardgivare("VG1", "Vardgivaren");
        
        Vardenhet ve1 = new Vardenhet("VE1", "Vardenhet 1");
        ve1.getMottagningar().add(new Mottagning("VE1M1", "Mottagning 11"));
        ve1.getMottagningar().add(new Mottagning("VE1M2", "Mottagning 12"));
        
        Vardenhet ve2 = new Vardenhet("VE2", "Vardenhet 2");
        ve2.getMottagningar().add(new Mottagning("VE2M1", "Mottagning 21"));
        ve2.getMottagningar().add(new Mottagning("VE2M2", "Mottagning 22"));
        
        vg1.getVardenheter().add(ve1);
        vg1.getVardenheter().add(ve2);
        
        return Arrays.asList(vg1);
    }
}
