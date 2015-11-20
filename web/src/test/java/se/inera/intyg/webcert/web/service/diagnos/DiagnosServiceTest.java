package se.inera.intyg.webcert.web.service.diagnos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.inera.certificate.common.enumerations.Diagnoskodverk;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponseType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/DiagnosService/DiagnosServiceTest-context.xml")
public class DiagnosServiceTest {

    @Autowired
    private DiagnosService service;

    private static final String ICD_10 = Diagnoskodverk.ICD_10_SE.name();

    private static final String KSH97P = Diagnoskodverk.KSH_97_P.name();

    @Test
    public void testGetICD10DiagnosisByCode() {
        assertEquals("Null should return invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode(null, ICD_10).getResultat());
        assertEquals("Empty should return invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode("", ICD_10).getResultat());
        assertEquals("Spaces should return invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode(" ", ICD_10).getResultat());
        assertEquals("A is too short and should  invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode("A", ICD_10).getResultat());
        assertEquals("A0 is too short and should return  invalid", DiagnosResponseType.INVALID_CODE,
                service.getDiagnosisByCode("A0", ICD_10).getResultat());
        assertEquals("X01.1X is syntactically correct but doesn't match anything in repo", DiagnosResponseType.NOT_FOUND,
                service.getDiagnosisByCode("X01.1X", ICD_10).getResultat());
        assertEquals("X00 is syntactically correct but doesn't match anything in repo", DiagnosResponseType.NOT_FOUND,
                service.getDiagnosisByCode("X00", ICD_10).getResultat());
        assertEquals("A00 should return a match", DiagnosResponseType.OK, service.getDiagnosisByCode("A00", ICD_10).getResultat());
        assertEquals("A000 should return a match", DiagnosResponseType.OK, service.getDiagnosisByCode("A000", ICD_10).getResultat());
        assertEquals("A00.0 should return a match", DiagnosResponseType.OK, service.getDiagnosisByCode("A00.0", ICD_10).getResultat());
        assertEquals("A083B should return a match", DiagnosResponseType.OK, service.getDiagnosisByCode("A00.0", ICD_10).getResultat());
        assertEquals("A08.3B should return a match", DiagnosResponseType.OK, service.getDiagnosisByCode("A00.0", ICD_10).getResultat());
        assertEquals("W0000 should return a match", DiagnosResponseType.OK, service.getDiagnosisByCode("W0000", ICD_10).getResultat());

    }

    @Test
    public void testGetKSH97PDiagnosisByCode() {
        assertEquals("Null should return invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode(null, KSH97P).getResultat());
        assertEquals("Empty should return invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode("", KSH97P).getResultat());
        assertEquals("Spaces should return invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode(" ", KSH97P).getResultat());
        assertEquals("A is too short and should  invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode("A", KSH97P).getResultat());
        assertEquals("A0 is too short and should return  invalid", DiagnosResponseType.INVALID_CODE,
                service.getDiagnosisByCode("A0", KSH97P).getResultat());
        assertEquals("X01-P is syntactically correct but doesn't match anything in repo", DiagnosResponseType.NOT_FOUND,
                service.getDiagnosisByCode("X01-P", KSH97P).getResultat());
        assertEquals("X00 is syntactically correct but doesn't match anything in repo", DiagnosResponseType.NOT_FOUND,
                service.getDiagnosisByCode("X00", KSH97P).getResultat());
        assertEquals("A00 should return a match", DiagnosResponseType.OK, service.getDiagnosisByCode("A00", KSH97P).getResultat());
        assertEquals("A000 should return a match", DiagnosResponseType.OK, service.getDiagnosisByCode("A000", KSH97P).getResultat());
        assertEquals("A09-P should return a match", DiagnosResponseType.OK, service.getDiagnosisByCode("A09-P", KSH97P).getResultat());
        assertEquals("A00- should return a match", DiagnosResponseType.OK, service.getDiagnosisByCode("A00-", KSH97P).getResultat());
        assertEquals("A00P should return a match", DiagnosResponseType.OK, service.getDiagnosisByCode("A000P", KSH97P).getResultat());

    }

    @Test
    public void testDiagnosServiceWithNbrOfResults() {
        DiagnosResponse res = service.searchDiagnosisByCode("A04", ICD_10, 5);
        assertNotNull(res);
        assertEquals(DiagnosResponseType.OK, res.getResultat());
        assertEquals("Should return codes A040-A045", 5, res.getDiagnoser().size());
    }

    @Test
    public void testDiagnosServiceWithNbrOfResults2() {
        DiagnosResponse res = service.searchDiagnosisByCode("A04", ICD_10, 10);
        assertNotNull(res);
        assertEquals(DiagnosResponseType.OK, res.getResultat());
        assertEquals(10, res.getDiagnoser().size());
    }

    @Test
    public void testDiagnosServiceWithNbrOfResults3() {
        DiagnosResponse res = service.searchDiagnosisByCode("A04", ICD_10, 15);
        assertNotNull(res);
        assertEquals(DiagnosResponseType.OK, res.getResultat());
        assertEquals(11, res.getDiagnoser().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDiagnosServiceWithNbrOfResultsZero() {
        service.searchDiagnosisByCode("A04", ICD_10, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDiagnosServiceWithNbrOfResultsMinusOne() {
        service.searchDiagnosisByCode("A04", ICD_10, -1);
    }

    @Test
    public void testValidateDiagnosService() {
        assertFalse("Null should return false", service.validateDiagnosisCode("null", ICD_10));
        assertFalse("Empty string should return false", service.validateDiagnosisCode("", ICD_10));
        assertFalse("Too short should return false", service.validateDiagnosisCode("A0", ICD_10));
        assertFalse("Wrong case should return false", service.validateDiagnosisCode("a04", ICD_10));
        assertTrue("Spaces after should not be a problem", service.validateDiagnosisCode("A04 ", ICD_10));

        assertTrue("A18 is a valid code", service.validateDiagnosisCode("A18", ICD_10));
        assertTrue("A184 is a valid code", service.validateDiagnosisCode("A184", ICD_10));
        assertTrue("A18.4 is a valid code", service.validateDiagnosisCode("A18.4", ICD_10));
        assertTrue("A184D is a valid code", service.validateDiagnosisCode("A184D", ICD_10));
        assertTrue("A18.4D is a valid code", service.validateDiagnosisCode("A18.4D", ICD_10));
        assertTrue("A1234 is a valid code", service.validateDiagnosisCode("A1234", ICD_10));
        assertTrue("A12.34 is a valid code", service.validateDiagnosisCode("A1234", ICD_10));
    }

    @Test
    public void testSearchDiagnosisByDescription() {
        DiagnosResponse res = service.searchDiagnosisByDescription("infekt", ICD_10, 5);
        assertNotNull(res);
        assertEquals(DiagnosResponseType.OK, res.getResultat());
        assertEquals("Should return 5 results", 5, res.getDiagnoser().size());
        assertEquals("A040", res.getDiagnoser().get(0).getKod());
        assertEquals("A041", res.getDiagnoser().get(1).getKod());
        assertEquals("A042", res.getDiagnoser().get(2).getKod());
        assertEquals("A043", res.getDiagnoser().get(3).getKod());
        assertEquals("A072", res.getDiagnoser().get(4).getKod());

        assertEquals("Null should return invalid", DiagnosResponseType.INVALID_SEARCH_STRING,
                service.searchDiagnosisByDescription(null, ICD_10, 5).getResultat());
        assertEquals("Empty should return invalid", DiagnosResponseType.INVALID_SEARCH_STRING,
                service.searchDiagnosisByDescription("", ICD_10, 5).getResultat());
        assertEquals("Spaces should return invalid", DiagnosResponseType.INVALID_SEARCH_STRING,
                service.searchDiagnosisByDescription(" ", ICD_10, 5).getResultat());
    }

    @Test
    public void testValidateDiagnosesCodeMissingKodverk() {
        boolean valid = service.validateDiagnosisCode("A18", null);
        assertFalse(valid);
    }
}
