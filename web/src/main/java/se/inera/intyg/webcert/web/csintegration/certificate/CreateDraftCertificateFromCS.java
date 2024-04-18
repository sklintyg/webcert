/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.csintegration.certificate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3.CreateDraftCertificateResponseFactory;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateDraftCertificateFromCS {

    private final PatientDetailsResolver patientDetailsResolver;
    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final IntegreradeEnheterRegistry integreradeEnheterRegistry;

    public CreateDraftCertificateResponseType create(Intyg certificate, IntygUser user) {
        final var applicationError = validatePatient(certificate);
        if (applicationError != null) {
            return applicationError;
        }

        final var modelId = csIntegrationService.certificateTypeExists(certificate.getTypAvIntyg().getCode());
        if (modelId.isEmpty()) {
            log.debug("Certificate type '{}' does not exist in certificate service", certificate.getTypAvIntyg().getCode());
            return null;
        }

        integreradeEnheterRegistry.putIntegreradEnhet(getIntegreradEnhetEntry(user), false, true);
        
        return csIntegrationService.createDraftCertificate(
            csIntegrationRequestFactory.createDraftCertificateRequest(
                modelId.get(), certificate, user
            )
        );
    }

    private static IntegreradEnhetEntry getIntegreradEnhetEntry(IntygUser user) {
        return new IntegreradEnhetEntry(user.getValdVardenhet().getId(),
            user.getValdVardenhet().getNamn(), user.getValdVardgivare().getId(), user.getValdVardgivare().getNamn());
    }

    private CreateDraftCertificateResponseType validatePatient(Intyg certificate) {
        final var personIdExtension = certificate.getPatient().getPersonId().getExtension();
        final var personId = Personnummer.createPersonnummer(personIdExtension)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Cannot create Personnummer object with invalid personId '%s'", personIdExtension)));

        final var sekretessStatus = patientDetailsResolver.getSekretessStatus(personId);

        if (sekretessStatus == SekretessStatus.UNDEFINED) {
            return CreateDraftCertificateResponseFactory.createErrorResponse(
                "Information om skyddade personuppgifter kunde inte hämtas. Intyget kan inte utfärdas"
                    + " för patient när uppgift om skyddade personuppgifter ej är tillgänglig.", ErrorIdType.APPLICATION_ERROR);
        }
        return null;
    }

}
