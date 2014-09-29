package se.inera.webcert.service.diagnos;

import java.util.List;

import se.inera.webcert.service.diagnos.dto.DiagnosResponse;
import se.inera.webcert.service.diagnos.dto.DiagnosResponseType;
import se.inera.webcert.service.diagnos.model.Diagnos;

/**
 * Supplies services related to diagnosises. 
 * 
 * @author npet
 *
 */
public interface DiagnosService {

    /**
     * Returns a Diagnos by its code.
     * 
     * @param code The code must at least correspond to the pattern 'A012' or 'A01.2'.
     * @return
     */
    public abstract DiagnosResponse getDiagnosisByCode(String code);

    /**
     * Searches the repository for codes beginning with the codeFragment
     * 
     * @param codeFragment The string to search codes by. The string must at least correspond to the pattern 'A01'.
     * @return
     */
    public abstract DiagnosResponse searchDiagnosisByCode(String codeFragment);
    
    /**
     *  Searches the repository for codes beginning with the codeFragment. Limits the number of matches returned.
     * 
     * @param codeFragment The string to search codes by. The string must at least correspond to the pattern 'A01'.
     * @param nbrOfResults The number of results to return, must be larger than 0.
     * @return
     */
    public abstract DiagnosResponse searchDiagnosisByCode(String codeFragment, int nbrOfResults);
    
    /**
     *  Validates that the supplied code fragment is syntactically correct.
     * 
     * @param codeFragment The code to be validated.
     * @return true if the code fragment is syntactically correct.
     */
    public abstract boolean validateDiagnosisCode(String codeFragment);

}
