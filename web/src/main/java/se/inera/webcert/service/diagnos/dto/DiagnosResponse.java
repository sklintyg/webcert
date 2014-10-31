package se.inera.webcert.service.diagnos.dto;

import java.util.List;

import se.inera.webcert.service.diagnos.model.Diagnos;

public class DiagnosResponse {

    private DiagnosResponseType resultat = DiagnosResponseType.OK;

    private Diagnos diagnos;

    private List<Diagnos> diagnoser;
    
    public DiagnosResponse() {
        
    }
    
    public static DiagnosResponse ok(Diagnos diagnos) {
        DiagnosResponse diagnosResponse = new DiagnosResponse();
        diagnosResponse.setDiagnos(diagnos);
        return diagnosResponse;
    }
    
    public static DiagnosResponse ok(List<Diagnos> diagnoser) {
        DiagnosResponse diagnosResponse = new DiagnosResponse();
        diagnosResponse.setDiagnoser(diagnoser);
        return diagnosResponse;
    }
    
    public static DiagnosResponse invalidCode() {
        DiagnosResponse diagnosResponse = new DiagnosResponse();
        diagnosResponse.setInvalidCode();
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
    
    private void setNotFound() {
        this.resultat = DiagnosResponseType.NOT_FOUND;
    }

    public Diagnos getDiagnos() {
        return diagnos;
    }

    public void setDiagnos(Diagnos diagnos) {
        this.diagnos = diagnos;
    }

    public List<Diagnos> getDiagnoser() {
        return diagnoser;
    }

    public void setDiagnoser(List<Diagnos> diagnoser) {
        this.diagnoser = diagnoser;
    }
}
