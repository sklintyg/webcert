package se.inera.intyg.webcert.web.service.diagnos.repo;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/DiagnosService/DiagnosRepositoryFactoryTest-context.xml")
public class DiagnosRepositoryTest {

    private static final String FILE_1 = "/DiagnosService/KSH97_TESTKODER_1.ANS";

    @Autowired
    private DiagnosRepositoryFactory factory;

    private DiagnosRepository repo;

    @Before
    public void setup() {
        DiagnosRepositoryImpl repoImpl = (DiagnosRepositoryImpl) factory.createAndInitDiagnosRepository(Collections.singletonList(FILE_1));
        assertEquals(100, repoImpl.nbrOfDiagosis());
        this.repo = repoImpl;
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
        List<Diagnos> res = repo.getDiagnosesByCode(null);
        assertTrue(res.isEmpty());

        res = repo.getDiagnosesByCode("");
        assertTrue(res.isEmpty());
    }

    @Test
    public void testGetByCodeFour() {
        String code = "A184";
        List<Diagnos> res = repo.getDiagnosesByCode(code);
        assertEquals(1, res.size());
        assertEquals("A184", res.get(0).getKod());
        assertThat(res.get(0).getBeskrivning(), containsString("Tuberkulos"));
    }

    @Test
    public void testGetByCodeFourAndDot() {
        String code = "A18.4";
        List<Diagnos> res = repo.getDiagnosesByCode(code);
        assertEquals(1, res.size());
        assertEquals("A184", res.get(0).getKod());
        assertThat(res.get(0).getBeskrivning(), containsString("Tuberkulos"));
    }

    @Test
    public void testGetByCodeFive() {
        String code = "A184E";
        List<Diagnos> res = repo.getDiagnosesByCode(code);
        assertEquals(1, res.size());
        assertEquals("A184E", res.get(0).getKod());
        assertThat(res.get(0).getBeskrivning(), containsString("Tuberkulöst"));
    }

    @Test
    public void testGetByCodeFiveAndDot() {
        String code = "A18.4E";
        List<Diagnos> res = repo.getDiagnosesByCode(code);
        assertEquals(1, res.size());
        assertEquals("A184E", res.get(0).getKod());
        assertThat(res.get(0).getBeskrivning(), containsString("Tuberkulöst"));
    }

    @Test
    public void testGetByCodeWithMalformedCode() {
        String code = " a 051  ";
        List<Diagnos> res = repo.getDiagnosesByCode(code);
        assertEquals(1, res.size());
        assertNotNull(res);
        assertEquals("A051", res.get(0).getKod());
    }

    @Test
    public void testSearchingWithFragmentThree() {
        String codeFragment = "A08";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertEquals(9, res.size());
    }

    @Test
    public void testSearchingWithFragmentFour() {
        String codeFragment = "A083";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertEquals(4, res.size());
    }

    @Test
    public void testSearchingWithFragmentFourAndDot() {
        String codeFragment = "A08.3";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertEquals(4, res.size());
    }

    @Test
    public void testSearchingWithFullCode() {
        String codeFragment = "A083B";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertEquals(1, res.size());
    }

    @Test
    public void testSearchingWithFullCodeAndDot() {
        String codeFragment = "A08.3B";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertEquals(1, res.size());
    }

    @Test
    public void testSearchingWithNonExistingFragment() {
        String codeFragment = "X01";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testSearchingWithNoInput() {
        String codeFragment = "";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testSearchingWithNull() {
        final String codeFragment = null;
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }
}
