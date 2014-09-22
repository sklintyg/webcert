package se.inera.webcert.service.diagnos;

import java.util.List;

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
     * @param code
     * @return
     */
    public abstract Diagnos getDiagnosisByCode(String code);

    /**
     * Searches the repository for codes beginning with the codeFragment
     * 
     * @param codeFragment The string to search codes by. An 'A0' will return all Diagnosises starting with this string.
     * @return
     */
    public abstract List<Diagnos> searchDiagnosisByCode(String codeFragment);
    
    /**
     *  Searches the repository for codes beginning with the codeFragment
     * 
     * @param codeFragment The string to search codes by. An 'A0' will return all Diagnosises starting with this string.
     * @param nbrOfResults The number of results to return. A -1 will return all results.
     * @return
     */
    public abstract List<Diagnos> searchDiagnosisByCode(String codeFragment, int nbrOfResults);

}
