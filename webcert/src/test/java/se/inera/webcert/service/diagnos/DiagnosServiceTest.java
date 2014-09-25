package se.inera.webcert.service.diagnos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import se.inera.webcert.service.diagnos.DiagnosService;
import se.inera.webcert.service.diagnos.DiagnosServiceImpl;
import se.inera.webcert.service.diagnos.model.Diagnos;

public class DiagnosServiceTest {

    private static DiagnosService service;
    
    private static final String FILE_1 = "/DiagnosService/KSH97_TESTKODER_1.ANS";
    
    @BeforeClass
    public static void setup() {
        DiagnosServiceImpl serviceImpl = new DiagnosServiceImpl(new String[]{ FILE_1 });
        serviceImpl.initDiagnosRepository();
        service = serviceImpl;
    }
    
    @Test
    public void testDiagnosServiceWithNbrOfResults() {
        
        List<Diagnos> res = service.searchDiagnosisByCode("A04", 5);
        assertNotNull(res);
        assertEquals("Should return codes A040-A045", 5, res.size());
    }
    
    @Test
    public void testDiagnosServiceWithNbrOfResults2() {
        
        List<Diagnos> res = service.searchDiagnosisByCode("A04", 10);
        assertNotNull(res);
        assertEquals(10, res.size());
    }
    
    @Test
    public void testDiagnosServiceWithNbrOfResults3() {
        
        List<Diagnos> res = service.searchDiagnosisByCode("A04", 15);
        assertNotNull(res);
        assertEquals(10, res.size());
    }
    
    @Test
    public void testDiagnosServiceWithNbrOfResultsZero() {
        
        List<Diagnos> res = service.searchDiagnosisByCode("A04", 0);
        
        assertNotNull(res);
        assertEquals(0, res.size());
    }
    
    @Test
    public void testDiagnosServiceWithNbrOfResultsNoLimit() {
        
        List<Diagnos> res = service.searchDiagnosisByCode("A04", -1);
        assertNotNull(res);
        assertEquals("Should return codes A040-A049", 10, res.size());
    }
    
    @Test
    public void testValidateDiagnosService() {
        assertFalse("Null should return false",service.validateDiagnosisCode("null", 3));
        assertFalse("Empty string should return false", service.validateDiagnosisCode("", 3));
        assertFalse("Too short should return false", service.validateDiagnosisCode("A0", 3));
        assertTrue(service.validateDiagnosisCode("A04", 3));
    }
}
