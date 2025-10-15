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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCandidateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.UpdateWithCandidateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
class UpdateCertificateFromCandidateServiceTest {

    private static final GetCandidateCertificateRequestDTO GET_CANDIDATE_CERTIFICATE_REQUEST = GetCandidateCertificateRequestDTO.builder()
        .build();
    private static final UpdateWithCandidateCertificateRequestDTO UPDATE_WITH_CANDIDATE_CERTIFICATE_REQUEST = UpdateWithCandidateCertificateRequestDTO.builder()
        .build();
    private static final String PATIENT_ID = "patientId";
    private static final Patient PATIENT = Patient.builder()
        .personId(
            PersonId.builder()
                .id(PATIENT_ID)
                .build()
        )
        .build();
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CANDIDATE_CERTIFICATE_ID = "candidateCertificateId";
    private static final Certificate CERTIFICATE = new Certificate();
    private static final Certificate CANDIDATE_CERTIFICATE = new Certificate();
    private static final String TYPE = "type";
    private static final String CANDIDATE_TYPE = "candidateType";

    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;

    @Mock
    PDLLogService pdlLogService;

    @Mock
    MonitoringLogService monitoringLogService;

    @Mock
    WebCertUserService webCertUserService;

    @Mock
    IntegrationParameters parameters;

    @Mock
    WebCertUser user;

    @Mock
    PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;

    @Mock
    PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;

    @Mock
    CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @InjectMocks
    UpdateCertificateFromCandidateInCertificateService updateCertificateFromCandidateInCertificateService;

    @Test
    void shouldReturnNullIfCertificateDoesNotExistInCS() {
        final var response = updateCertificateFromCandidateInCertificateService.update(CERTIFICATE_ID);

        assertNull(response);
    }

    @Nested
    class CertificateExistsInCS {

        @BeforeEach
        void setup() {
            CANDIDATE_CERTIFICATE.setMetadata(CertificateMetadata.builder()
                .id(CANDIDATE_CERTIFICATE_ID)
                .type(CANDIDATE_TYPE)
                .patient(PATIENT)
                .build());

            CERTIFICATE.setMetadata(CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
                .type(TYPE)
                .patient(PATIENT)
                .build());

            when(csIntegrationService.certificateExists(anyString()))
                .thenReturn(true);

            when(csIntegrationRequestFactory.getCandidateCertificateRequest())
                .thenReturn(GET_CANDIDATE_CERTIFICATE_REQUEST);

            when(csIntegrationService.getCandidateCertificate(anyString(), any()))
                .thenReturn(CANDIDATE_CERTIFICATE);

            when(csIntegrationRequestFactory.updateWithCandidateCertificateRequestDTO(PATIENT_ID, parameters))
                .thenReturn(UPDATE_WITH_CANDIDATE_CERTIFICATE_REQUEST);

            when(csIntegrationService.updateWithCandidateCertificate(CERTIFICATE_ID, CANDIDATE_CERTIFICATE_ID,
                UPDATE_WITH_CANDIDATE_CERTIFICATE_REQUEST)).thenReturn(CERTIFICATE);

            when(user.getParameters())
                .thenReturn(parameters);

            when(webCertUserService.getUser())
                .thenReturn(user);

            final var valdVardenhet = new Vardenhet();
            valdVardenhet.setId("vardenhetId");
            when(user.getValdVardenhet())
                .thenReturn(valdVardenhet);
            when(user.getHsaId())
                .thenReturn("hsaId");
        }

        @Test
        void shouldReturnUpdatedCertificateId() {
            final var response = updateCertificateFromCandidateInCertificateService.update(CERTIFICATE_ID);
            assertEquals(CERTIFICATE_ID, response);
        }

        @Test
        void shouldPdlLogReadForCandidateCertificate() {
            updateCertificateFromCandidateInCertificateService.update(CERTIFICATE_ID);
            verify(pdlLogService).logRead(CANDIDATE_CERTIFICATE);
        }

        @Test
        void shouldPdlLogSavedForCertificate() {
            updateCertificateFromCandidateInCertificateService.update(CERTIFICATE_ID);
            verify(pdlLogService).logSaved(CERTIFICATE);
        }

        @Test
        void shouldPublishCreated() {
            updateCertificateFromCandidateInCertificateService.update(CERTIFICATE_ID);
            verify(publishCertificateStatusUpdateService).publish(CERTIFICATE, HandelsekodEnum.ANDRAT);
        }

        @Test
        void shouldMonitorLogUtkastCreatedTemplateAuto() {
            updateCertificateFromCandidateInCertificateService.update(CERTIFICATE_ID);
            verify(monitoringLogService).logUtkastCreatedTemplateAuto(
                CERTIFICATE_ID,
                TYPE,
                user.getHsaId(),
                user.getValdVardenhet().getId(),
                CANDIDATE_CERTIFICATE_ID,
                CANDIDATE_TYPE
            );
        }

        @Test
        void shouldPublishAnalyticsMessageWhenCertificateIsReplaced() {
            final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
            when(certificateAnalyticsMessageFactory.draftUpdatedFromCertificate(CERTIFICATE)).thenReturn(analyticsMessage);

            updateCertificateFromCandidateInCertificateService.update(CERTIFICATE_ID);

            verify(publishCertificateAnalyticsMessage).publishEvent(analyticsMessage);
        }
    }
}
