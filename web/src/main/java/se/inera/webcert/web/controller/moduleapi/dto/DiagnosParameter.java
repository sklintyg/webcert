package se.inera.webcert.web.controller.moduleapi.dto;

/**
 * Parameter object for DiagnosService.
 *
 * @author npet
 *
 */
public class DiagnosParameter {

    private String codeFragment;

    // This will by default return all matches
    private int nbrOfResults = -1;

    public DiagnosParameter() {

    }

    public String getCodeFragment() {
        return codeFragment;
    }

    public void setCodeFragment(String codeFragment) {
        this.codeFragment = codeFragment;
    }

    public int getNbrOfResults() {
        return nbrOfResults;
    }

    public void setNbrOfResults(int nbrOfResults) {
        this.nbrOfResults = nbrOfResults;
    }
}
