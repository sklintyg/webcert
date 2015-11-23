package se.inera.intyg.webcert.web.service.log;

import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;

public final class LogRequestFactory {

    private LogRequestFactory() {
    }

    public static LogRequest createLogRequestFromUtkast(Utkast utkast) {

        LogRequest logRequest = new LogRequest();

        logRequest.setIntygId(utkast.getIntygsId());
        logRequest.setPatientId(utkast.getPatientPersonnummer());
        logRequest.setPatientName(utkast.getPatientFornamn(), utkast.getPatientMellannamn(), utkast.getPatientEfternamn());

        logRequest.setIntygCareUnitId(utkast.getEnhetsId());
        logRequest.setIntygCareUnitName(utkast.getEnhetsNamn());

        logRequest.setIntygCareGiverId(utkast.getVardgivarId());
        logRequest.setIntygCareGiverName(utkast.getVardgivarNamn());

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
