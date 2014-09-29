package se.inera.webcert.service.diagnos.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import se.inera.webcert.service.diagnos.model.Diagnos;

public class DiagnosRepositoryTest {
    
    private static DiagnosRepository repo;
    
    private static final String FILE_1 = "/DiagnosService/KSH97_TESTKODER_1.ANS";
    
    @BeforeClass
    public static void setup() {
        DiagnosRepositoryFactory factory = new DiagnosRepositoryFactory(Arrays.asList(FILE_1));
        DiagnosRepositoryImpl repoImpl = (DiagnosRepositoryImpl) factory.createAndInitDiagnosRepository();
        assertEquals(100, repoImpl.nbrOfDiagosis());
        repo = repoImpl;
    }
    
    @Test
    public void testSanitizeCodeValue() {
        DiagnosRepositoryImpl repoImpl = new DiagnosRepositoryImpl();
        assertNull("null should return null", repoImpl.sanitizeCodeValue(null));
        assertNull("emptry string should return null", repoImpl.sanitizeCodeValue(""));
        assertNull("spaces should return null", repoImpl.sanitizeCodeValue("  "));
        assertNull(". should return null", repoImpl.sanitizeCodeValue("."));
        assertNull(". and spaces should return null", repoImpl.sanitizeCodeValue(". "));
        assertEquals("A", repoImpl.sanitizeCodeValue("A"));
        assertEquals("A", repoImpl.sanitizeCodeValue("a"));
        assertEquals("A", repoImpl.sanitizeCodeValue(" A "));
        assertEquals("A1", repoImpl.sanitizeCodeValue("A1"));
        assertEquals("A1", repoImpl.sanitizeCodeValue(" A1 "));
        assertEquals("A1", repoImpl.sanitizeCodeValue(" A 1 "));
        assertEquals("A1", repoImpl.sanitizeCodeValue("A.1"));
        assertEquals("A11", repoImpl.sanitizeCodeValue("A1.1"));
        assertEquals("A11", repoImpl.sanitizeCodeValue(" A1 1"));
        assertEquals("A111", repoImpl.sanitizeCodeValue("A11.1"));
        assertEquals("A111", repoImpl.sanitizeCodeValue(" A11.1 "));
        assertEquals("A111", repoImpl.sanitizeCodeValue("a11.1"));
        assertEquals("A111", repoImpl.sanitizeCodeValue("A11.1"));
        assertEquals("A111", repoImpl.sanitizeCodeValue("A 11.1"));
    }
    
    @Test
    public void testGetByCodeWithNullAndEmpty() {
        Diagnos res = repo.getDiagnosByCode(null);
        assertNull(res);
        
        res = repo.getDiagnosByCode("");
        assertNull(res);
    }
    
    @Test
    public void testGetByCodeFour() {
        String code = "A184";
        Diagnos res = repo.getDiagnosByCode(code);
        assertEquals("A184", res.getKod());
        assertThat(res.getBeskrivning(), containsString("Tuberkulos"));
    }
    
    @Test
    public void testGetByCodeFourAndDot() {
        String code = "A18.4";
        Diagnos res = repo.getDiagnosByCode(code);
        assertEquals("A184", res.getKod());
        assertThat(res.getBeskrivning(), containsString("Tuberkulos"));
    }
    
    @Test
    public void testGetByCodeFive() {
        String code = "A184E";
        Diagnos res = repo.getDiagnosByCode(code);
        assertEquals("A184E", res.getKod());
        assertThat(res.getBeskrivning(), containsString("Tuberkulöst"));
    }
    
    @Test
    public void testGetByCodeFiveAndDot() {
        String code = "A18.4E";
        Diagnos res = repo.getDiagnosByCode(code);
        assertEquals("A184E", res.getKod());
        assertThat(res.getBeskrivning(), containsString("Tuberkulöst"));
    }
        
    @Test
    public void testGetByCodeWithMalformedCode() {
        String code = " a 051  ";
        Diagnos res = repo.getDiagnosByCode(code);
        assertNotNull(res);
        assertEquals("A051", res.getKod());
    }
    
    @Test
    public void testSearchingWithFragmentThree() {
        
        String codeFragment = "A08";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment);
        assertEquals(9, res.size());
    }
    
    @Test
    public void testSearchingWithFragmentFour() {
        
        String codeFragment = "A083";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment);
        assertEquals(4, res.size());
    }
    
    @Test
    public void testSearchingWithFragmentFourAndDot() {
        
        String codeFragment = "A08.3";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment);
        assertEquals(4, res.size());
    }
    
    @Test
    public void testSearchingWithFullCode() {
        
        String codeFragment = "A083B";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment);
        assertEquals(1, res.size());
    }
    
    @Test
    public void testSearchingWithFullCodeAndDot() {
        
        String codeFragment = "A08.3B";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment);
        assertEquals(1, res.size());
    }
        
    @Test
    public void testSearchingWithNonExistingFragment() {
        
        String codeFragment = "X01";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }
    
    @Test
    public void testSearchingWithNoInput() {
        
        String codeFragment = "";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }
    
    @Test
    public void testSearchingWithNull() {
        
        String codeFragment = null;
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }
}
