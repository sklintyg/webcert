package se.inera.webcert.service.diagnos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import se.inera.webcert.service.diagnos.DiagnosService;
import se.inera.webcert.service.diagnos.DiagnosServiceImpl;
import se.inera.webcert.service.diagnos.dto.DiagnosResponse;
import se.inera.webcert.service.diagnos.dto.DiagnosResponseType;
import se.inera.webcert.service.diagnos.model.Diagnos;

public class DiagnosServiceTest {

    private static DiagnosService service;

    private static final String FILE_1 = "/DiagnosService/KSH97_TESTKODER_1.ANS";

    @BeforeClass
    public static void setup() {
        DiagnosServiceImpl serviceImpl = new DiagnosServiceImpl(new String[] { FILE_1 });
        serviceImpl.initDiagnosRepository();
        service = serviceImpl;
    }

    @Test
    public void testGetDiagnosisByCode() {
        assertEquals("Null should return invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode(null).getResultat());
        assertEquals("Empty should return invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode("").getResultat());
        assertEquals("Spaces should return invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode(" ").getResultat());
        assertEquals("A is too short and should  invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode("A").getResultat());
        assertEquals("A0 is too short and should return  invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode("A0").getResultat());
        assertEquals("A00 is syntactically correct but doesn't match anything in repo", DiagnosResponseType.NOT_FOUND, service.getDiagnosisByCode("A00").getResultat());
        assertEquals("X01.1X is syntactically correct but doesn't match anything in repo", DiagnosResponseType.NOT_FOUND, service.getDiagnosisByCode("X01.1X").getResultat());
        assertEquals("A000 should return a match", DiagnosResponseType.OK, service.getDiagnosisByCode("A000").getResultat());
        assertEquals("A00.0 should return a match", DiagnosResponseType.OK, service.getDiagnosisByCode("A00.0").getResultat());
        assertEquals("A083B should return a match", DiagnosResponseType.OK, service.getDiagnosisByCode("A00.0").getResultat());
        assertEquals("A08.3B should return a match", DiagnosResponseType.OK, service.getDiagnosisByCode("A00.0").getResultat());
    }

    @Test
    public void testDiagnosServiceWithNbrOfResults() {
        DiagnosResponse res = service.searchDiagnosisByCode("A04", 5);
        assertNotNull(res);
        assertEquals(DiagnosResponseType.OK, res.getResultat());
        assertEquals("Should return codes A040-A045", 5, res.getDiagnoser().size());
    }

    @Test
    public void testDiagnosServiceWithNbrOfResults2() {
        DiagnosResponse res = service.searchDiagnosisByCode("A04", 10);
        assertNotNull(res);
        assertEquals(DiagnosResponseType.OK, res.getResultat());
        assertEquals(10, res.getDiagnoser().size());
    }

    @Test
    public void testDiagnosServiceWithNbrOfResults3() {
        DiagnosResponse res = service.searchDiagnosisByCode("A04", 15);
        assertNotNull(res);
        assertEquals(DiagnosResponseType.OK, res.getResultat());
        assertEquals(10, res.getDiagnoser().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDiagnosServiceWithNbrOfResultsZero() {
        service.searchDiagnosisByCode("A04", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDiagnosServiceWithNbrOfResultsMinusOne() {
        service.searchDiagnosisByCode("A04", -1);
    }

    @Test
    public void testValidateDiagnosService() {
        assertFalse("Null should return false", service.validateDiagnosisCode("null"));
        assertFalse("Empty string should return false", service.validateDiagnosisCode(""));
        assertFalse("Too short should return false", service.validateDiagnosisCode("A0"));
        assertFalse("Wrong case should return false", service.validateDiagnosisCode("a04"));
        assertTrue("Spaces after should not be a problem", service.validateDiagnosisCode("A04 "));

        assertTrue("A18 is a valid code", service.validateDiagnosisCode("A18"));
        assertTrue("A184 is a valid code", service.validateDiagnosisCode("A184"));
        assertTrue("A18.4 is a valid code", service.validateDiagnosisCode("A18.4"));
        assertTrue("A184D is a valid code", service.validateDiagnosisCode("A184D"));
        assertTrue("A18.4D is a valid code", service.validateDiagnosisCode("A18.4D"));
    }
}
