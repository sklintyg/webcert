package se.inera.webcert.service.log;

import se.inera.certificate.model.Id;
import se.inera.certificate.model.Patient;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.Vardenhet;
import se.inera.certificate.model.Vardgivare;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.service.log.dto.LogRequest;

public final class LogRequestFactory {

    private LogRequestFactory() {
    }

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

    public static LogRequest createLogRequestFromExternalModel(Utlatande utlatande) {

        LogRequest logRequest = new LogRequest();
        logRequest.setIntygId(extractIntygIdFromId(utlatande.getId()));

        Patient patient = utlatande.getPatient();

        logRequest.setPatientId(patient.getId().getExtension());

        logRequest.setPatientName(patient.getFullstandigtNamn());

        Vardenhet skapadAvVardenhet = utlatande.getSkapadAv().getVardenhet();

        logRequest.setIntygCareUnitId(skapadAvVardenhet.getId().getExtension());
        logRequest.setIntygCareUnitName(skapadAvVardenhet.getNamn());

        Vardgivare skapadAvVardgivare = skapadAvVardenhet.getVardgivare();

        logRequest.setIntygCareGiverId(skapadAvVardgivare.getId().getExtension());
        logRequest.setIntygCareGiverName(skapadAvVardgivare.getNamn());

        return logRequest;
    }

    private static String extractIntygIdFromId(Id id) {

        if (id.getExtension() != null) {
            return id.getExtension();
        }

        return id.getRoot();
    }
}
