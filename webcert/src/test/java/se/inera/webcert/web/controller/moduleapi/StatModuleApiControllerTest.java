package se.inera.webcert.web.controller.moduleapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyListOf;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
    
    @Captor
    private ArgumentCaptor<List<String>> listCaptor;
    
    @InjectMocks 
    private StatModuleApiController statController;

    private WebCertUser mockUser;
    
    private Map<String, Long> statsMap;
    
    private Vardgivare vg;
    
    private Vardenhet ve1, ve2, ve3, ve4;
    
    @Before
    public void setupDataAndExpectations() {
        
        statsMap = new HashMap<String, Long>(); 
        
        statsMap.put("VE1", new Long(2L));
        statsMap.put("VE1M1", new Long(3L));
        statsMap.put("VE1M2", new Long(3L));
        statsMap.put("VE2", new Long(2L));
        statsMap.put("VE3", new Long(1L));
                
        mockUser = new WebCertUser();
        
        ve1 = new Vardenhet("VE1", "Vardenhet1");
        ve1.getMottagningar().add(new Mottagning("VE1M1", "Mottagning1"));
        ve1.getMottagningar().add(new Mottagning("VE1M2", "Mottagning2"));
        
        ve2 = new Vardenhet("VE2", "Vardenhet2");
        ve2.getMottagningar().add(new Mottagning("VE2M1", "Mottagning3"));
        
        ve3 = new Vardenhet("VE3", "Vardenhet3");
        
        ve4 = new Vardenhet("VE4", "Vardenhet4");
        
        vg = new Vardgivare("VG1", "Vardgivaren");
        vg.setVardenheter(Arrays.asList(ve1, ve2, ve3, ve4));
        
        mockUser.setVardgivare(Arrays.asList(vg));
        mockUser.setValdVardgivare(vg);
        
        Mockito.when(webCertUserService.getWebCertUser()).thenReturn(mockUser);
    }
    
    @Test
    public void testGetStatisticsWithSelectedUnitVE2() {
  
        mockUser.setValdVardenhet(ve2);
        
        Mockito.when(fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(anyListOf(String.class))).thenReturn(statsMap);
                
        Response response = statController.getStatistics();
        
        Mockito.verify(webCertUserService).getWebCertUser();
                
        Mockito.verify(fragaSvarService).getNbrOfUnhandledFragaSvarForCareUnits(listCaptor.capture());
                
        List<String> listArgs = listCaptor.getValue();
        assertEquals(7, listArgs.size());
        
        assertNotNull(response);
        assertEquals(OK, response.getStatus());
        
        StatsResponse statsResponse = (StatsResponse) response.getEntity();
        assertNotNull(statsResponse);
        
        assertEquals(2, statsResponse.getTotalNbrOfUnhandledFragaSvarOnSelected());
        assertEquals(9, statsResponse.getTotalNbrOfUnhandledFragaSvarOnOtherThanSelected());
    }
    
    @Test
    public void testGetStatisticsWithSelectedUnitVE3() {
  
        mockUser.setValdVardenhet(ve3);
        
        Mockito.when(fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(anyListOf(String.class))).thenReturn(statsMap);
                
        Response response = statController.getStatistics();
        
        Mockito.verify(webCertUserService).getWebCertUser();
                
        Mockito.verify(fragaSvarService).getNbrOfUnhandledFragaSvarForCareUnits(listCaptor.capture());
                
        List<String> listArgs = listCaptor.getValue();
        assertEquals(7, listArgs.size());
        
        assertNotNull(response);
        assertEquals(OK, response.getStatus());
        
        StatsResponse statsResponse = (StatsResponse) response.getEntity();
        assertNotNull(statsResponse);
        
        assertEquals(1, statsResponse.getTotalNbrOfUnhandledFragaSvarOnSelected());
        assertEquals(10, statsResponse.getTotalNbrOfUnhandledFragaSvarOnOtherThanSelected());
    }
    
    @Test
    public void testGetStatisticsWithSelectedUnitVE4() {
  
        mockUser.setValdVardenhet(ve4);
        
        Mockito.when(fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(anyListOf(String.class))).thenReturn(statsMap);
                
        Response response = statController.getStatistics();
        
        Mockito.verify(webCertUserService).getWebCertUser();
                
        Mockito.verify(fragaSvarService).getNbrOfUnhandledFragaSvarForCareUnits(listCaptor.capture());
                
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
        
        Mockito.when(fragaSvarService.getNbrOfUnhandledFragaSvarForCareUnits(anyListOf(String.class))).thenReturn(statsMap);
                
        Response response = statController.getStatistics();
        
        Mockito.verify(webCertUserService).getWebCertUser();
                
        Mockito.verify(fragaSvarService).getNbrOfUnhandledFragaSvarForCareUnits(listCaptor.capture());
                
        List<String> listArgs = listCaptor.getValue();
        assertEquals(7, listArgs.size());
        
        assertNotNull(response);
        assertEquals(OK, response.getStatus());
        
        StatsResponse statsResponse = (StatsResponse) response.getEntity();
        assertNotNull(statsResponse);
        
        assertEquals(8, statsResponse.getTotalNbrOfUnhandledFragaSvarOnSelected());
        assertEquals(3, statsResponse.getTotalNbrOfUnhandledFragaSvarOnOtherThanSelected());
        
        StatsResponse refStatsResponse = getReference("StatModuleApiControllerTest/reference.json");
        assertEquals(refStatsResponse.toString(), statsResponse.toString());
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
