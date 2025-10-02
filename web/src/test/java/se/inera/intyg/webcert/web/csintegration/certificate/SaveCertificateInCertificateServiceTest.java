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
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@ExtendWith(MockitoExtension.class)
class SaveCertificateInCertificateServiceTest {

    @Mock
    PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @Mock
    PDLLogService pdlLogService;
    @Mock
    MonitoringLogService monitoringLogService;
    @Mock
    PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    @Mock
    CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;
    @InjectMocks
    SaveCertificateInCertificateService saveCertificateInCertificateService;

    private static final String CERTIFICATE_ID = "ID";
    private static final String CERTIFICATE_TYPE = "TYPE";
    private static final Certificate CERTIFICATE = new Certificate();
    private static final boolean PDL_LOG = true;
    private static final SaveCertificateRequestDTO REQUEST = SaveCertificateRequestDTO.builder().build();
    private static final long VERSION_FROM_CS = 99L;

    private static final String ID = "patientId";

    static {
        CERTIFICATE.setMetadata(
            CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
                .type(CERTIFICATE_TYPE)
                .version(VERSION_FROM_CS)
                .patient(
                    Patient.builder()
                        .personId(
                            PersonId.builder()
                                .id(ID)
                                .build()
                        )
                        .build()
                )
                .build()
        );
    }

    @Test
    void shouldReturnNullIfCertificateDoesNotExistInCS() {
        assertEquals(
            -1L,
            saveCertificateInCertificateService.saveCertificate(CERTIFICATE, PDL_LOG)
        );
    }

    @Test
    void shouldNotPerformPDLLogIfTypeWasNotRetrievedFromCS() {
        saveCertificateInCertificateService.saveCertificate(CERTIFICATE, PDL_LOG);
        verifyNoInteractions(pdlLogService);
    }

    @Nested
    class CertificateServiceHasCertificate {

        @BeforeEach
        void setup() {
            when(csIntegrationService.certificateExists(CERTIFICATE_ID))
                .thenReturn(true);
            when(csIntegrationService.saveCertificate(REQUEST))
                .thenReturn(CERTIFICATE);
            when(csIntegrationRequestFactory.saveRequest(CERTIFICATE, ID))
                .thenReturn(REQUEST);
        }

        @Test
        void shouldReturnCertificate() {
            assertEquals(VERSION_FROM_CS,
                saveCertificateInCertificateService.saveCertificate(CERTIFICATE, PDL_LOG)
            );
        }

        @Test
        void shouldPerformPDLForCreateCertificateIfPdlLogIsTrue() {
            saveCertificateInCertificateService.saveCertificate(CERTIFICATE, PDL_LOG);
            verify(pdlLogService, times(1)).logSaved(CERTIFICATE);
        }

        @Test
        void shouldNotPerformPDLForCreateCertificateIfPdlLogIsFalse() {
            saveCertificateInCertificateService.saveCertificate(CERTIFICATE, false);
            verifyNoInteractions(pdlLogService);
        }

        @Test
        void shouldPerformMonitorLogForUtkastEdited() {
            saveCertificateInCertificateService.saveCertificate(CERTIFICATE, PDL_LOG);
            verify(monitoringLogService, times(1)).logUtkastEdited(CERTIFICATE_ID, CERTIFICATE_TYPE);
        }

        @Test
        void shouldPublishCertificateStatusUpdate() {
            saveCertificateInCertificateService.saveCertificate(CERTIFICATE, PDL_LOG);
            verify(publishCertificateStatusUpdateService, times(1)).publish(CERTIFICATE, HandelsekodEnum.ANDRAT);
        }

        @Test
        void shouldPublishAnalyticsMessage() {
            final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
            when(certificateAnalyticsMessageFactory.draftUpdated(CERTIFICATE)).thenReturn(analyticsMessage);

            saveCertificateInCertificateService.saveCertificate(CERTIFICATE, PDL_LOG);

            verify(publishCertificateAnalyticsMessage, times(1)).publishEvent(analyticsMessage);
        }
    }
}
