package se.inera.intyg.webcert.web.service.diagnos;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DiagnosServiceImplTest {

    @Test
    public void testValidateDiagnosisCodeIcd10() throws Exception {
        // Verifies that the comment "Tested with: A11, A11.1, A11.1X, A111, A111X, A1111"
        // for se.inera.intyg.webcert.web.service.diagnos.DiagnosServiceImpl.ICD10_CODE_REGEXP is true

        //Given
        final List<String> codes = Arrays.asList("A11", "A11.1", "A11.1X", "A111", "A111X", "A1111");

        for (String code : codes) {
            //When
            final boolean result = new DiagnosServiceImpl().validateDiagnosisCode(code, "ICD_10_SE");

            //Then
            assertTrue(result);
        }
    }

    @Test
    public void testValidateDiagnosisCodeKsh97p() throws Exception {
        // Verifies that the comment "Tested with: A11, A11-P, A11-, A111, A111P"
        // for se.inera.intyg.webcert.web.service.diagnos.DiagnosServiceImpl.KSH97P_CODE_REGEXP is true

        //Given
        final List<String> codes = Arrays.asList("A11", "A11-P", "A11-", "A111", "A111P");

        for (String code : codes) {
            //When
            final boolean result = new DiagnosServiceImpl().validateDiagnosisCode(code, "KSH_97_P");

            //Then
            assertTrue(result);
        }
    }

}
