package se.inera.intyg.webcert.web.service.diagnos.dto;

import java.util.List;

import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;

public class DiagnosResponse {

    private DiagnosResponseType resultat = DiagnosResponseType.OK;

    private List<Diagnos> diagnoser;

    private boolean moreResults;

    public DiagnosResponse() {

    }

    public static DiagnosResponse ok(List<Diagnos> diagnoser,  boolean moreResults) {
        DiagnosResponse diagnosResponse = new DiagnosResponse();
        diagnosResponse.setDiagnoser(diagnoser);
        diagnosResponse.setMoreResults(moreResults);
        return diagnosResponse;
    }

    public static DiagnosResponse invalidCode() {
        DiagnosResponse diagnosResponse = new DiagnosResponse();
        diagnosResponse.setInvalidCode();
        return diagnosResponse;
    }

    public static DiagnosResponse invalidCodesystem() {
        DiagnosResponse diagnosResponse = new DiagnosResponse();
        diagnosResponse.setInvalidCodesystem();
        return diagnosResponse;
    }

    public static DiagnosResponse invalidSearchString() {
        DiagnosResponse diagnosResponse = new DiagnosResponse();
        diagnosResponse.setInvalidSearchString();
        return diagnosResponse;
    }

    public static DiagnosResponse notFound() {
        DiagnosResponse diagnosResponse = new DiagnosResponse();
        diagnosResponse.setNotFound();
        return diagnosResponse;
    }

    public DiagnosResponseType getResultat() {
        return resultat;
    }

    private void setInvalidCode() {
        this.resultat = DiagnosResponseType.INVALID_CODE;
    }

    private void setInvalidCodesystem() {
        this.resultat = DiagnosResponseType.INVALID_CODE_SYSTEM;
    }

    private void setInvalidSearchString() {
        this.resultat = DiagnosResponseType.INVALID_SEARCH_STRING;
    }

    private void setNotFound() {
        this.resultat = DiagnosResponseType.NOT_FOUND;
    }

    public List<Diagnos> getDiagnoser() {
        return diagnoser;
    }

    public void setDiagnoser(List<Diagnos> diagnoser) {
        this.diagnoser = diagnoser;
    }

    public boolean isMoreResults() {
        return moreResults;
    }

    public void setMoreResults(boolean moreResults) {
        this.moreResults = moreResults;
    }
}
