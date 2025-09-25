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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.facade.impl.CreateCertificateException;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@ExtendWith(MockitoExtension.class)
class CreateCertificateFromCertificateServiceTest {

    @Mock
    PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    @Mock
    MonitoringLogService monitoringLogService;
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @Mock
    PDLLogService pdlLogService;
    @Mock
    PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    @Mock
    CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @InjectMocks
    CreateCertificateFromCertificateService createCertificateFromCertificateService;

    private static final Certificate CERTIFICATE = new Certificate();
    private static final String CERTIFICATE_ID = "ID";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String UNIT_ID = "unitId";
    private static final String HSA_ID = "hsaId";
    private static final String PATIENT_ID = "s191212121212";
    private static final String TYPE = "TYPE";
    private static final String VERSION = "VERSION";
    private static final CertificateModelIdDTO CERTIFICATE_MODEL_ID = CertificateModelIdDTO.builder()
        .type(TYPE)
        .version(VERSION)
        .build();
    private static final CreateCertificateRequestDTO REQUEST = CreateCertificateRequestDTO.builder().build();

    static {
        CERTIFICATE.setMetadata(
            CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
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
                .build()
        );
    }

    @Test
    void shouldThrowExceptionIfCertificateTypeExistsThrows() {
        doThrow(new IllegalStateException()).when(csIntegrationService).certificateTypeExists(TYPE);

        assertThrows(IllegalStateException.class,
            () -> createCertificateFromCertificateService.create(TYPE, PATIENT_ID)
        );
    }

    @Test
    void shouldReturnNullIfCertificateTypeDoesntExist() throws CreateCertificateException {
        doReturn(Optional.empty()).when(csIntegrationService).certificateTypeExists(TYPE);

        assertNull(
            createCertificateFromCertificateService.create(TYPE, PATIENT_ID)
        );
    }

    @Test
    void shouldNotPerformPDLLogIfTypeWasNotCreatedFromCS() throws CreateCertificateException {
        doReturn(Optional.empty()).when(csIntegrationService).certificateTypeExists(TYPE);
        createCertificateFromCertificateService.create(TYPE, PATIENT_ID);
        verifyNoInteractions(pdlLogService);
    }

    @Nested
    class CertificateTypeExists {

        @BeforeEach
        void setUp() {
            doReturn(Optional.of(CERTIFICATE_MODEL_ID)).when(csIntegrationService).certificateTypeExists(TYPE);
        }

        @Test
        void shouldThrowCertificateCreateExceptionIfCreateCertificateThrows() {
            doReturn(REQUEST).when(csIntegrationRequestFactory).createCertificateRequest(CERTIFICATE_MODEL_ID, PATIENT_ID);
            doThrow(new IllegalStateException()).when(csIntegrationService).createCertificate(REQUEST);

            assertThrows(CreateCertificateException.class,
                () -> createCertificateFromCertificateService.create(TYPE, PATIENT_ID)
            );
        }

        @Test
        void shouldThrowCertificateCreateExceptionIfCreateCertificateRequestThrows() {
            doThrow(new IllegalStateException()).when(csIntegrationRequestFactory)
                .createCertificateRequest(CERTIFICATE_MODEL_ID, PATIENT_ID);

            assertThrows(CreateCertificateException.class,
                () -> createCertificateFromCertificateService.create(TYPE, PATIENT_ID)
            );
        }

        @Test
        void shouldPerformPDLForCreateCertificate() throws CreateCertificateException {
            doReturn(REQUEST).when(csIntegrationRequestFactory).createCertificateRequest(CERTIFICATE_MODEL_ID, PATIENT_ID);
            doReturn(CERTIFICATE).when(csIntegrationService).createCertificate(REQUEST);

            createCertificateFromCertificateService.create(TYPE, PATIENT_ID);

            verify(pdlLogService, times(1)).logCreated(CERTIFICATE);
        }

        @Test
        void shouldPerformMonitorLogForCreateCertificate() throws CreateCertificateException {
            doReturn(REQUEST).when(csIntegrationRequestFactory).createCertificateRequest(CERTIFICATE_MODEL_ID, PATIENT_ID);
            doReturn(CERTIFICATE).when(csIntegrationService).createCertificate(REQUEST);

            createCertificateFromCertificateService.create(TYPE, PATIENT_ID);

            verify(monitoringLogService, times(1)).logUtkastCreated(
                CERTIFICATE_ID, CERTIFICATE_TYPE, UNIT_ID, HSA_ID, 0
            );
        }

        @Test
        void shouldPublishCertificateStatusUpdateService() throws CreateCertificateException {
            doReturn(REQUEST).when(csIntegrationRequestFactory).createCertificateRequest(CERTIFICATE_MODEL_ID, PATIENT_ID);
            doReturn(CERTIFICATE).when(csIntegrationService).createCertificate(REQUEST);

            createCertificateFromCertificateService.create(TYPE, PATIENT_ID);

            verify(publishCertificateStatusUpdateService, times(1)).publish(
                CERTIFICATE, HandelsekodEnum.SKAPAT
            );
        }

        @Test
        void shouldPublishAnalyticsMessageWhenDraftIsCreated() throws CreateCertificateException {
            doReturn(REQUEST).when(csIntegrationRequestFactory).createCertificateRequest(CERTIFICATE_MODEL_ID, PATIENT_ID);
            doReturn(CERTIFICATE).when(csIntegrationService).createCertificate(REQUEST);
            final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
            when(certificateAnalyticsMessageFactory.draftCreated(CERTIFICATE)).thenReturn(analyticsMessage);

            createCertificateFromCertificateService.create(TYPE, PATIENT_ID);

            verify(publishCertificateAnalyticsMessage, times(1)).publishEvent(analyticsMessage);
        }

        @Test
        void shouldReturnIdOfCertificate() throws CreateCertificateException {
            doReturn(REQUEST).when(csIntegrationRequestFactory).createCertificateRequest(CERTIFICATE_MODEL_ID, PATIENT_ID);
            doReturn(CERTIFICATE).when(csIntegrationService).createCertificate(REQUEST);

            assertEquals(CERTIFICATE_ID,
                createCertificateFromCertificateService.create(TYPE, PATIENT_ID)
            );
        }
    }
}
