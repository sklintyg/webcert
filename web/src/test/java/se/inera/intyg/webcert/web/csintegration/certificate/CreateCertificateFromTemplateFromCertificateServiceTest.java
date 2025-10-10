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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateFromTemplateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateFromTemplateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
class CreateCertificateFromTemplateFromCertificateServiceTest {

    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @Mock
    MonitoringLogServiceImpl monitoringLogService;
    @Mock
    PDLLogService pdlLogService;
    @Mock
    PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    @Mock
    CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;
    @Mock
    PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    @Mock
    WebCertUserService webCertUserService;
    @Mock
    IntegrationParameters parameters;

    @InjectMocks
    CreateCertificateFromTemplateFromCertificateService createCertificateFromTemplateFromCertificateService;

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String NEW_CERTIFICATE_ID = "newCertificateId";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String ORIGINAL_CERTIFICATE_TYPE = "originalCertificateType";
    private static final String UNIT_ID = "unitId";
    private static final String HSA_ID = "hsaId";
    private static final CreateCertificateFromTemplateRequestDTO REQUEST = CreateCertificateFromTemplateRequestDTO.builder().build();
    private static final GetCertificateRequestDTO GET_CERTIFICATE_REQUEST = GetCertificateRequestDTO.builder().build();
    private static final Certificate CERTIFICATE = new Certificate();
    private static final Certificate ORIGINAL_CERTIFICATE = new Certificate();
    private static final CreateCertificateFromTemplateResponseDTO RESPONSE = CreateCertificateFromTemplateResponseDTO.builder()
        .certificate(CERTIFICATE)
        .build();
    private static final String PATIENT_ID = "patientId";

    @BeforeEach
    void setUp() {
        CERTIFICATE.setMetadata(
            CertificateMetadata.builder()
                .id(NEW_CERTIFICATE_ID)
                .type(CERTIFICATE_TYPE)
                .unit(
                    Unit.builder()
                        .unitId(UNIT_ID)
                        .build()
                )
                .issuedBy(
                    Staff.builder()
                        .personId(HSA_ID)
                        .build()
                )
                .patient(
                    Patient.builder()
                        .personId(
                            PersonId.builder()
                                .id(PATIENT_ID)
                                .build()
                        )
                        .build()
                )
                .build()
        );

        ORIGINAL_CERTIFICATE.setMetadata(
            CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
                .type(ORIGINAL_CERTIFICATE_TYPE)
                .patient(
                    Patient.builder()
                        .personId(
                            PersonId.builder()
                                .id(PATIENT_ID)
                                .build()
                        )
                        .build()
                )
                .build()
        );
    }

    @Test
    void shouldReturnNullIfCertificateDoesNotExistInCS() {
        when(csIntegrationService.certificateExists(CERTIFICATE_ID))
            .thenReturn(false);

        final var result = createCertificateFromTemplateFromCertificateService.createCertificateFromTemplate(CERTIFICATE_ID);

        verify(csIntegrationService, times(1)).certificateExists(CERTIFICATE_ID);
        verifyNoInteractions(csIntegrationRequestFactory);
        assertNull(result);
    }

    @Nested
    class CertificateExistsInCS {

        @BeforeEach
        void setUp() {
            when(csIntegrationService.certificateExists(CERTIFICATE_ID))
                .thenReturn(true);
            when(csIntegrationRequestFactory.createCertificateFromTemplateRequest(PATIENT_ID, parameters))
                .thenReturn(REQUEST);
            when(csIntegrationRequestFactory.getCertificateRequest())
                .thenReturn(GET_CERTIFICATE_REQUEST);
            when(csIntegrationService.getCertificate(CERTIFICATE_ID, GET_CERTIFICATE_REQUEST))
                .thenReturn(ORIGINAL_CERTIFICATE);
          final var user = mock(WebCertUser.class);
          when(webCertUserService.getUser()).thenReturn(user);
          when(user.getParameters()).thenReturn(parameters);
        }

        @Test
        void shouldReturnNewCertificateIdWhenSuccessful() {
            when(csIntegrationService.createDraftFromCertificate(CERTIFICATE_ID, REQUEST))
                .thenReturn(RESPONSE);

            final var result = createCertificateFromTemplateFromCertificateService.createCertificateFromTemplate(CERTIFICATE_ID);

            verify(csIntegrationService, times(1)).certificateExists(CERTIFICATE_ID);
            verify(csIntegrationRequestFactory, times(1)).createCertificateFromTemplateRequest(PATIENT_ID, parameters);
            verify(csIntegrationService, times(1)).createDraftFromCertificate(CERTIFICATE_ID, REQUEST);
            assertEquals(NEW_CERTIFICATE_ID, result);
        }

        @Test
        void shouldPerformPDLLoggingForCreatedCertificate() {
            when(csIntegrationService.createDraftFromCertificate(CERTIFICATE_ID, REQUEST))
                .thenReturn(RESPONSE);

            createCertificateFromTemplateFromCertificateService.createCertificateFromTemplate(CERTIFICATE_ID);

            verify(pdlLogService, times(1)).logCreated(CERTIFICATE);
        }

        @Test
        void shouldPublishAnalyticsMessageWhenCertificateIsCreatedFromTemplate() {
            when(csIntegrationService.createDraftFromCertificate(CERTIFICATE_ID, REQUEST))
                .thenReturn(RESPONSE);
            final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
            when(certificateAnalyticsMessageFactory.draftCreatedFromCertificate(CERTIFICATE))
                .thenReturn(analyticsMessage);

            createCertificateFromTemplateFromCertificateService.createCertificateFromTemplate(CERTIFICATE_ID);

            verify(publishCertificateAnalyticsMessage, times(1)).publishEvent(analyticsMessage);
        }

        @Test
        void shouldPerformMonitoringLoggingForTemplateCreation() {
            when(csIntegrationService.createDraftFromCertificate(CERTIFICATE_ID, REQUEST))
                .thenReturn(RESPONSE);

            createCertificateFromTemplateFromCertificateService.createCertificateFromTemplate(CERTIFICATE_ID);

            verify(monitoringLogService, times(1)).logUtkastCreatedTemplateManual(
                NEW_CERTIFICATE_ID,
                CERTIFICATE_TYPE,
                HSA_ID,
                UNIT_ID,
                CERTIFICATE_ID,
                ORIGINAL_CERTIFICATE_TYPE
            );
        }

        @Test
        void shouldPublishCertificateStatusUpdateWithSkapatEvent() {
            when(csIntegrationService.createDraftFromCertificate(CERTIFICATE_ID, REQUEST))
                .thenReturn(RESPONSE);

            createCertificateFromTemplateFromCertificateService.createCertificateFromTemplate(CERTIFICATE_ID);

            verify(publishCertificateStatusUpdateService, times(1)).publish(
                CERTIFICATE, HandelsekodEnum.SKAPAT
            );
        }

        @Test
        void shouldNotPerformAnyLoggingWhenCSReturnsNull() {
            when(csIntegrationService.createDraftFromCertificate(CERTIFICATE_ID, REQUEST))
                .thenReturn(null);

            createCertificateFromTemplateFromCertificateService.createCertificateFromTemplate(CERTIFICATE_ID);

            verifyNoInteractions(pdlLogService);
            verifyNoInteractions(publishCertificateAnalyticsMessage);
            verifyNoInteractions(monitoringLogService);
            verifyNoInteractions(publishCertificateStatusUpdateService);
        }

        @Test
        void shouldNotPerformAnyLoggingWhenCSReturnsResponseWithNullCertificate() {
            final var emptyResponse = CreateCertificateFromTemplateResponseDTO.builder()
                .certificate(null)
                .build();
            when(csIntegrationService.createDraftFromCertificate(CERTIFICATE_ID, REQUEST))
                .thenReturn(emptyResponse);

            createCertificateFromTemplateFromCertificateService.createCertificateFromTemplate(CERTIFICATE_ID);

            verifyNoInteractions(pdlLogService);
            verifyNoInteractions(publishCertificateAnalyticsMessage);
            verifyNoInteractions(monitoringLogService);
            verifyNoInteractions(publishCertificateStatusUpdateService);
        }

        @Test
        void shouldReturnNullWhenCSReturnsNull() {
            when(csIntegrationService.createDraftFromCertificate(CERTIFICATE_ID, REQUEST))
                .thenReturn(null);

            final var result = createCertificateFromTemplateFromCertificateService.createCertificateFromTemplate(CERTIFICATE_ID);

            verify(csIntegrationService, times(1)).certificateExists(CERTIFICATE_ID);
            verify(csIntegrationRequestFactory, times(1)).createCertificateFromTemplateRequest(PATIENT_ID, parameters);
            verify(csIntegrationService, times(1)).createDraftFromCertificate(CERTIFICATE_ID, REQUEST);
            assertNull(result);
        }

        @Test
        void shouldReturnNullWhenCSReturnsResponseWithNullCertificate() {
            final var emptyResponse = CreateCertificateFromTemplateResponseDTO.builder()
                .certificate(null)
                .build();
            when(csIntegrationService.createDraftFromCertificate(CERTIFICATE_ID, REQUEST))
                .thenReturn(emptyResponse);

            final var result = createCertificateFromTemplateFromCertificateService.createCertificateFromTemplate(CERTIFICATE_ID);

            verify(csIntegrationService, times(1)).certificateExists(CERTIFICATE_ID);
            verify(csIntegrationRequestFactory, times(1)).createCertificateFromTemplateRequest(PATIENT_ID, parameters);
            verify(csIntegrationService, times(1)).createDraftFromCertificate(CERTIFICATE_ID, REQUEST);
            assertNull(result);
        }

        @Test
        void shouldPropagateExceptionWhenCSIntegrationServiceThrows() {
            when(csIntegrationService.createDraftFromCertificate(CERTIFICATE_ID, REQUEST))
                .thenThrow(new IllegalStateException("CS error"));

            try {
                createCertificateFromTemplateFromCertificateService.createCertificateFromTemplate(CERTIFICATE_ID);
            } catch (IllegalStateException e) {
                assertEquals("CS error", e.getMessage());
            }

            verify(csIntegrationService, times(1)).certificateExists(CERTIFICATE_ID);
            verify(csIntegrationRequestFactory, times(1)).createCertificateFromTemplateRequest(PATIENT_ID, parameters);
            verify(csIntegrationService, times(1)).createDraftFromCertificate(CERTIFICATE_ID, REQUEST);
        }
    }
}
