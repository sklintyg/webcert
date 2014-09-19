package se.inera.webcert.web.controller.moduleapi.dto;

import java.util.List;

import se.inera.webcert.service.diagnos.model.Diagnos;

public class DiagnosResponse {

    private DiagnosResponseType resultat = DiagnosResponseType.OK;

    private Diagnos diagnos;

    private List<Diagnos> diagnoser;
    
    public DiagnosResponse() {
        
    }

    public DiagnosResponseType getResultat() {
        return resultat;
    }

    public void setResultatCodeTooShort() {
        this.resultat = DiagnosResponseType.CODE_TO_SHORT;
    }
    
    public void setResultatNotFound() {
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

    private enum DiagnosResponseType {
        NOT_FOUND,
        CODE_TO_SHORT,
        OK;
    };
}
