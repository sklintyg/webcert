package se.inera.intyg.webcert.web.service.diagnos;

import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;

/**
 * Supplies services related to diagnosises.
 *
 * @author npet
 *
 */
public interface DiagnosService {

    /**
     * Returns all diagnoses in the repository exactly matching the code.
     *
     * @param code
     *            The code to search for.
     * @param codeSystem
     *            A String representing the code system to which the code belongs.
     */
    DiagnosResponse getDiagnosisByCode(String code, String codeSystem);

    /**
     * Returns all diagnoses in the repository exactly matching the code.
     *
     * @param code
     *            The code to search for.
     * @param codeSystem
     *            The code system to which the code belongs (i.e ICD-10-SE or KSH97P)
     */
    DiagnosResponse getDiagnosisByCode(String code, Diagnoskodverk codeSystem);

    /**
     * Searches the repository for codes beginning with the codeFragment. Limits the number of matches returned.
     *
     * @param codeFragment
     *            The string to search codes by. The string must at least correspond to the pattern 'A01'.
     * @param codeSystem
     *            The code system to which the code belongs (i.e ICD-10-SE or KSH97P)
     * @param nbrOfResults
     *            The number of results to return, must be larger than 0.
     */
    DiagnosResponse searchDiagnosisByCode(String codeFragment, String codeSystem, int nbrOfResults);

    /**
     * Searches the repository for descriptions matching searchString. Limits the number of matches returned.
     *
     * @param searchString
     *            The string to search for in descriptions.
     * @param codeSystem
     *            The code system to which the code belongs (i.e ICD-10-SE or KSH97P)
     * @param nbrOfResults
     *            The number of results to return, must be larger than 0.
     */
    DiagnosResponse searchDiagnosisByDescription(String searchString, String codeSystem, int nbrOfResults);

    /**
     * Validates that the supplied code fragment is syntactically correct.
     *
     * @param codeFragment
     *            The code to be validated.
     * @param codeSystem
     *            The code system to which the code belongs (i.e ICD-10-SE or KSH97P)
     * @return true if the code fragment is syntactically correct.
     */
    boolean validateDiagnosisCode(String codeFragment, String codeSystem);
}
