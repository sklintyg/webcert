package se.inera.webcert.service.log;

import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.service.log.dto.LogRequest;

public class LogRequestFactory {

    public static LogRequest createLogRequestFromDraft(Intyg draft) {

        LogRequest logRequest = new LogRequest();

        logRequest.setIntygId(draft.getIntygsId());
        logRequest.setPatientId(draft.getPatientPersonnummer());
        logRequest.setPatientName(draft.getPatientFornamn(), draft.getPatientMellannamn(), draft.getPatientEfternamn());

        logRequest.setIntygCareUnitId(draft.getEnhetsId());
        logRequest.setIntygCareUnitName(draft.getEnhetsNamn());

        logRequest.setIntygCareGiverId(draft.getVardgivarId());
        logRequest.setIntygCareGiverName(draft.getVardgivarNamn());

        return logRequest;
    }

    public static LogRequest createLogRequestFromUtlatande(Utlatande utlatande) {

        LogRequest logRequest = new LogRequest();
        logRequest.setIntygId(utlatande.getId());

        logRequest.setPatientId(utlatande.getGrundData().getPatient().getPersonId());
        logRequest.setPatientName(utlatande.getGrundData().getPatient().getFullstandigtNamn());

        logRequest.setIntygCareUnitId(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsid());
        logRequest.setIntygCareUnitName(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn());

        logRequest.setIntygCareGiverId(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid());
        logRequest.setIntygCareGiverName(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarnamn());

        return logRequest;
    }
}
