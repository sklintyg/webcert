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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3.CreateDraftCertificateResponseFactory.createErrorResponse;
import static se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3.CreateDraftCertificateResponseFactory.createSuccessResponse;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.Forbidden;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.web.csintegration.exception.HandleApiErrorService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3.CreateDraftCertificate;
import se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3.CreateDraftCertificateResponseFactory;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.LoggedInWebcertUserFactory;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateDraftCertificateFromCS implements CreateDraftCertificate {

    private static final int NO_PREFILL_ELEMENTS = 0;
    private final PatientDetailsResolver patientDetailsResolver;
    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final IntegratedUnitRegistryHelper integratedUnitRegistryHelper;
    private final PDLLogService pdlLogService;
    private final MonitoringLogService monitoringLogService;
    private final HandleApiErrorService handleApiErrorService;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    private final LoggedInWebcertUserFactory loggedInWebcertUserFactory;
    private final CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;
    private final PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;

    @Override
    public CreateDraftCertificateResponseType create(Intyg certificate, IntygUser user) {
        final var modelId = csIntegrationService.certificateExternalTypeExists(
            certificate.getTypAvIntyg().getCodeSystem(), certificate.getTypAvIntyg().getCode()
        );

        if (modelId.isEmpty()) {
            log.debug("Certificate type '{}' does not exist in certificate service", certificate.getTypAvIntyg().getCode());
            return null;
        }

        final var applicationError = validatePUIntegration(certificate);
        if (applicationError.isPresent()) {
            return applicationError.get();
        }

        integratedUnitRegistryHelper.addUnit(user);

        try {
            final var draftCertificateRequest = csIntegrationRequestFactory.createDraftCertificateRequest(modelId.get(), certificate, user);
            final var createdCertificate = csIntegrationService.createCertificate(
                draftCertificateRequest
            );

            pdlLogService.logCreatedWithIntygUser(createdCertificate, user);
            monitoringLogService.logUtkastCreated(
                createdCertificate.getMetadata().getId(),
                createdCertificate.getMetadata().getType(),
                createdCertificate.getMetadata().getUnit().getUnitId(),
                createdCertificate.getMetadata().getIssuedBy().getPersonId(),
                NO_PREFILL_ELEMENTS
            );

            publishCertificateStatusUpdateService.publish(createdCertificate, HandelsekodEnum.SKAPAT, Optional.of(user), Optional.empty());

            final var loggedInWebcertUser = loggedInWebcertUserFactory.create(user);
            publishCertificateAnalyticsMessage.publishEvent(
                draftCertificateRequest.getPrefillXml() == null ?
                    certificateAnalyticsMessageFactory.draftCreated(createdCertificate, loggedInWebcertUser) :
                    certificateAnalyticsMessageFactory.draftCreatedWithPrefill(createdCertificate, loggedInWebcertUser)
            );

            return createSuccessResponse(createdCertificate.getMetadata().getId(), createdCertificate.getMetadata().getUnit().getUnitId());
        } catch (Forbidden exception) {
            final var errorReason = handleApiErrorService.handle(exception.getResponseBodyAsString());
            log.warn(
                String.format(
                    "User '%s' on unit '%s' received error when creating draft: '%s'",
                    user.getHsaId(),
                    user.getValdVardenhet(),
                    errorReason
                ),
                exception
            );
            return createErrorResponse(errorReason, ErrorIdType.VALIDATION_ERROR);
        } catch (Exception exception) {
            log.error(
                String.format(
                    "User '%s' on unit '%s' could not create draft",
                    user.getHsaId(),
                    user.getValdVardenhet()
                ),
                exception
            );
            return createErrorResponse("Internal error. Could not create draft", ErrorIdType.APPLICATION_ERROR);
        }
    }

    private Optional<CreateDraftCertificateResponseType> validatePUIntegration(Intyg certificate) {
        final var personIdExtension = certificate.getPatient().getPersonId().getExtension();
        final var personId = Personnummer.createPersonnummer(personIdExtension)
            .orElseThrow(() -> new WebCertServiceException(
                WebCertServiceErrorCodeEnum.PU_PROBLEM,
                String.format("Cannot create Personnummer object with invalid personId '%s'", personIdExtension))
            );

        final var sekretessStatus = patientDetailsResolver.getSekretessStatus(personId);

        if (sekretessStatus == SekretessStatus.UNDEFINED) {
            return Optional.of(
                CreateDraftCertificateResponseFactory.createErrorResponse(
                    "Information om skyddade personuppgifter kunde inte hämtas. Intyget kan inte utfärdas"
                        + " för patient när uppgift om skyddade personuppgifter ej är tillgänglig.", ErrorIdType.APPLICATION_ERROR)
            );
        }
        return Optional.empty();
    }

}