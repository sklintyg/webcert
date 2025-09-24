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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRecipient;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SendCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@ExtendWith(MockitoExtension.class)
class SendCertificateFromCertificateServiceTest {

    private static final SendCertificateRequestDTO REQUEST = SendCertificateRequestDTO.builder().build();
    private static final String ID = "ID";
    private static final Certificate CERTIFICATE = new Certificate();
    private static final String TYPE = "TYPE";
    private static final String RECIPIENT_ID = "FKASSA";

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
    SendCertificateFromCertificateService sendCertificateFromCertificateService;

    @Test
    void shouldReturnNullIfCertificateDoesNotExistInCS() {
        final var response = sendCertificateFromCertificateService.sendCertificate(ID);

        assertNull(response);
    }

    @Nested
    class CertificateExistsInCS {

        @BeforeEach
        void setup() {

            CERTIFICATE.setMetadata(CertificateMetadata.builder()
                .id(ID)
                .type(TYPE)
                .recipient(
                    CertificateRecipient.builder()
                        .id(RECIPIENT_ID)
                        .build()
                )
                .build());

            when(csIntegrationService.certificateExists(ID))
                .thenReturn(true);

            when(csIntegrationRequestFactory.sendCertificateRequest())
                .thenReturn(REQUEST);
        }

        @Nested
        class CertificateIsSentFromCS {

            @BeforeEach
            void setup() {
                when(csIntegrationService.sendCertificate(ID, REQUEST))
                    .thenReturn(CERTIFICATE);
            }

            @Test
            void shouldCallSendWithId() {
                final var captor = ArgumentCaptor.forClass(String.class);
                sendCertificateFromCertificateService.sendCertificate(ID);

                verify(csIntegrationService).sendCertificate(captor.capture(), any(SendCertificateRequestDTO.class));
                assertEquals(ID, captor.getValue());
            }

            @Test
            void shouldCallSendWithRequest() {
                final var captor = ArgumentCaptor.forClass(SendCertificateRequestDTO.class);
                sendCertificateFromCertificateService.sendCertificate(ID);

                verify(csIntegrationService).sendCertificate(anyString(), captor.capture());
                assertEquals(REQUEST, captor.getValue());
            }

            @Test
            void shouldPdlLogSent() {
                sendCertificateFromCertificateService.sendCertificate(ID);
                verify(pdlLogService).logSent(CERTIFICATE);

            }

            @Test
            void shouldMonitorLogSent() {
                sendCertificateFromCertificateService.sendCertificate(ID);
                verify(monitoringLogService).logIntygSent(ID, TYPE, RECIPIENT_ID);
            }

            @Test
            void shouldPublishCertificateStatusUpdate() {
                sendCertificateFromCertificateService.sendCertificate(ID);
                verify(publishCertificateStatusUpdateService).publish(CERTIFICATE, HandelsekodEnum.SKICKA);
            }

            @Test
            void shouldPublishCertificateAnalyticsMessage() {
                final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
                when(certificateAnalyticsMessageFactory.certificateSent(CERTIFICATE)).thenReturn(analyticsMessage);
                sendCertificateFromCertificateService.sendCertificate(ID);
                verify(publishCertificateAnalyticsMessage).publishEvent(analyticsMessage);
            }

            @Test
            void shouldReturnOK() {
                assertEquals(IntygServiceResult.OK.toString(), sendCertificateFromCertificateService.sendCertificate(ID));
            }
        }

        @Test
        void shouldThrowExceptionIfReturnedCertificateIsNull() {
            assertThrows(IllegalStateException.class, () -> sendCertificateFromCertificateService.sendCertificate(ID));
        }
    }
}
