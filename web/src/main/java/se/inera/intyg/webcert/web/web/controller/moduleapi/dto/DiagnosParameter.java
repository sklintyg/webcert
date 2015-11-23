package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

/**
 * Parameter object for DiagnosService.
 *
 * @author npet
 *
 */
public class DiagnosParameter {

    private String codeFragment;
    private String codeSystem;

    private String descriptionSearchString;

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

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public String getDescriptionSearchString() {
        return descriptionSearchString;
    }

    public void setDescriptionSearchString(String descriptionSearchString) {
        this.descriptionSearchString = descriptionSearchString;
    }

    public int getNbrOfResults() {
        return nbrOfResults;
    }

    public void setNbrOfResults(int nbrOfResults) {
        this.nbrOfResults = nbrOfResults;
    }
}
