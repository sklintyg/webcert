/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.service.log.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * Service / Factory that produces PDL log requests.
 *
 * Note the hard-coded business rule where intygstyper having FKASSA as default recipient will have patient
 * first, middle and last names blanked out.
 *
 * @author eriklupander
 */
@Service
@RequiredArgsConstructor
public class LogRequestFactoryImpl implements LogRequestFactory {

    private static final String SJF_LOG_POST = "Läsning i enlighet med sammanhållen journalföring";

    private final PatientDetailsResolver patientDetailsResolver;

    @Override
    public LogRequest createLogRequestFromUtkast(Utkast utkast) {
        return createLogRequestFromUtkast(utkast, false);
    }

    @Override
    public LogRequest createLogRequestFromUtkast(Utkast utkast, boolean sjf) {
        final var logRequest = LogRequest.builder()
            .intygId(utkast.getIntygsId())
            .testIntyg(utkast.isTestIntyg() || isPatientTestIndicated(utkast.getPatientPersonnummer()))
            .patientId(utkast.getPatientPersonnummer())
            .intygCareUnitId(utkast.getEnhetsId())
            .intygCareUnitName(utkast.getEnhetsNamn())
            .intygCareGiverId(utkast.getVardgivarId())
            .intygCareGiverName(utkast.getVardgivarNamn());

        if (sjf) {
            logRequest.additionalInfo(SJF_LOG_POST);
        }

        return logRequest.build();
    }

    @Override
    public LogRequest createLogRequestFromUtlatande(Utlatande utlatande) {
        return createLogRequestFromUtlatande(utlatande, false);
    }

    @Override
    public LogRequest createLogRequestFromUtlatande(Utlatande utlatande, boolean sjf) {
        return createLogRequestFromUtlatande(utlatande, sjf ? SJF_LOG_POST : null);
    }

    @Override
    public LogRequest createLogRequestFromUtlatande(Utlatande utlatande, String additionalInfo) {
        return LogRequest.builder()
            .intygId(utlatande.getId())
            .testIntyg(
                utlatande.getGrundData().isTestIntyg() || isPatientTestIndicated(utlatande.getGrundData().getPatient().getPersonId())
            )
            .patientId(utlatande.getGrundData().getPatient().getPersonId())
            .intygCareUnitId(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsid())
            .intygCareUnitName(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn())
            .intygCareGiverId(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid())
            .intygCareGiverName(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarnamn())
            .additionalInfo(additionalInfo)
            .build();
    }

    @Override
    public LogRequest createLogRequestFromUser(WebCertUser user, String patientId) {
        return createLogRequestFromUser(user, patientId, null);
    }

    @Override
    public LogRequest createLogRequestFromUser(WebCertUser user, String patientId, String intygsId) {
        final var personnummer = getPersonnummer(patientId);

        final var logRequest = LogRequest.builder()
            .intygId(intygsId)
            .patientId(personnummer)
            .testIntyg(isPatientTestIndicated(personnummer))
            .intygCareUnitId(user.getValdVardenhet().getId())
            .intygCareUnitName(user.getValdVardenhet().getNamn())
            .intygCareGiverId(user.getValdVardgivare().getId())
            .intygCareGiverName(user.getValdVardgivare().getNamn());

        if (user.getParameters() != null && user.getParameters().isSjf()) {
            logRequest.additionalInfo(SJF_LOG_POST);
        }

        return logRequest.build();
    }

    @Override
    public LogRequest createLogRequestFromCertificate(Certificate certificate, String additionalInfo) {
        final var personId = certificate.getMetadata().getPatient().getActualPersonId().getId();
        final var personnummer = getPersonnummer(personId);

        final var logRequest = LogRequest.builder()
            .intygId(certificate.getMetadata().getId())
            .testIntyg(certificate.getMetadata().isTestCertificate() || isPatientTestIndicated(personnummer))
            .patientId(personnummer)
            .intygCareUnitId(certificate.getMetadata().getUnit().getUnitId())
            .intygCareUnitName(certificate.getMetadata().getUnit().getUnitName())
            .intygCareGiverId(certificate.getMetadata().getCareProvider().getUnitId())
            .intygCareGiverName(certificate.getMetadata().getCareProvider().getUnitName());

        if (additionalInfo != null) {
            logRequest.additionalInfo(additionalInfo);
        }

        return logRequest.build();
    }

    private static Personnummer getPersonnummer(String personId) {
        return Personnummer.createPersonnummer(personId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    String.format("PatientId has wrong format: '%s'", personId)
                )
            );
    }

    private boolean isPatientTestIndicated(Personnummer personnummer) {
        return patientDetailsResolver.isTestIndicator(personnummer);
    }
}
