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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateXmlRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateXmlResponseDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@ExtendWith(MockitoExtension.class)
class DeleteCertificateFromCertificateServiceTest {

    private static final DeleteCertificateRequestDTO REQUEST = DeleteCertificateRequestDTO.builder().build();
    private static final String ID = "ID";
    private static final int VERSION = 10;
    private static final Certificate CERTIFICATE = new Certificate();
    private static final String TYPE = "TYPE";
    private static final String XML_DATA = "xmlData";
    private static final GetCertificateXmlResponseDTO XML_RESPONSE_DTO = GetCertificateXmlResponseDTO.builder()
        .xml(XML_DATA)
        .build();
    private static final GetCertificateXmlRequestDTO XML_REQUEST_DTO = GetCertificateXmlRequestDTO.builder().build();

    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;

    @Mock
    PDLLogService pdlLogService;

    @Mock
    PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    @Mock
    MonitoringLogService monitoringLogService;
    @Mock
    PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    @Mock
    CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @InjectMocks
    DeleteCertificateFromCertificateService deleteCertificateFromCertificateService;

    @Test
    void shouldReturnFalseIfCertificateDoesNotExistInCS() {
        final var response = deleteCertificateFromCertificateService.deleteCertificate(ID, VERSION);

        assertFalse(response);
    }

    @Nested
    class CertificateExistsInCS {

        @BeforeEach
        void setup() {

            CERTIFICATE.setMetadata(CertificateMetadata.builder()
                .id(ID)
                .type(TYPE)
                .build());

            when(csIntegrationService.certificateExists(ID))
                .thenReturn(true);

            when(csIntegrationService.getCertificateXml(XML_REQUEST_DTO, ID))
                .thenReturn(XML_RESPONSE_DTO);

            when(csIntegrationRequestFactory.getCertificateXmlRequest())
                .thenReturn(XML_REQUEST_DTO);

            when(csIntegrationRequestFactory.deleteCertificateRequest())
                .thenReturn(REQUEST);
        }

        @Nested
        class CertificateIsDeletedFromCS {

            @BeforeEach
            void setup() {
                when(csIntegrationService.deleteCertificate(ID, VERSION, REQUEST))
                    .thenReturn(CERTIFICATE);
            }

            @Test
            void shouldCallDeleteWithVersion() {
                final var captor = ArgumentCaptor.forClass(Long.class);
                deleteCertificateFromCertificateService.deleteCertificate(ID, VERSION);

                verify(csIntegrationService).deleteCertificate(anyString(), captor.capture(), any(DeleteCertificateRequestDTO.class));
                assertEquals(VERSION, captor.getValue());
            }

            @Test
            void shouldCallDeleteWithId() {
                final var captor = ArgumentCaptor.forClass(String.class);
                deleteCertificateFromCertificateService.deleteCertificate(ID, VERSION);

                verify(csIntegrationService).deleteCertificate(captor.capture(), anyLong(), any(DeleteCertificateRequestDTO.class));
                assertEquals(ID, captor.getValue());
            }

            @Test
            void shouldCallDeleteWithRequest() {
                final var captor = ArgumentCaptor.forClass(DeleteCertificateRequestDTO.class);
                deleteCertificateFromCertificateService.deleteCertificate(ID, VERSION);

                verify(csIntegrationService).deleteCertificate(anyString(), anyLong(), captor.capture());
                assertEquals(REQUEST, captor.getValue());
            }

            @Test
            void shouldPdlLogDelete() {
                deleteCertificateFromCertificateService.deleteCertificate(ID, VERSION);
                verify(pdlLogService).logDeleted(CERTIFICATE);

            }

            @Test
            void shouldMonitorLogDelete() {
                deleteCertificateFromCertificateService.deleteCertificate(ID, VERSION);
                verify(monitoringLogService).logUtkastDeleted(ID, TYPE);
            }

            @Test
            void shouldCallPublishCertificateStatusServiceWithXmlData() {
                deleteCertificateFromCertificateService.deleteCertificate(ID, VERSION);
                verify(publishCertificateStatusUpdateService).publish(CERTIFICATE, HandelsekodEnum.RADERA, XML_DATA);
            }

            @Test
            void shouldPublishAnalyticsMessageWhenCertificateIsDeleted() {
                final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
                when(certificateAnalyticsMessageFactory.deleted(CERTIFICATE)).thenReturn(analyticsMessage);

                deleteCertificateFromCertificateService.deleteCertificate(ID, VERSION);

                verify(publishCertificateAnalyticsMessage).publishEvent(analyticsMessage);
            }
        }

        @Test
        void shouldThrowExceptionIfReturnedCertificateIsNull() {
            assertThrows(IllegalStateException.class, () -> deleteCertificateFromCertificateService.deleteCertificate(ID, VERSION));
        }
    }
}
