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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ReadyForSignRequestDTO;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@ExtendWith(MockitoExtension.class)
class ReadyForSignForCertificateServiceTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String TYPE = "type";
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @Mock
    PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    @Mock
    MonitoringLogService monitoringLogService;
    @Mock
    DecorateCertificateFromCSWithInformationFromWC decorateCertificateFromCSWithInformationFromWC;
    @InjectMocks
    ReadyForSignForCertificateService readyForSignForCertificateService;

    @Test
    void shallReturnNullIfCertificateDoesNotExistIsCertificateService() {
        doReturn(false).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        final var result = readyForSignForCertificateService.readyForSign(CERTIFICATE_ID);
        assertNull(result);
    }

    @Test
    void shallReturnCertificate() {
        final var expectedCertificate = getCertificate();
        final var readyForSignRequestDTO = ReadyForSignRequestDTO.builder().build();
        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(readyForSignRequestDTO).when(csIntegrationRequestFactory).readyForSignRequest();
        doReturn(expectedCertificate).when(csIntegrationService).markCertificateReadyForSign(CERTIFICATE_ID, readyForSignRequestDTO);

        final var actualCertificate = readyForSignForCertificateService.readyForSign(CERTIFICATE_ID);
        assertEquals(expectedCertificate, actualCertificate);
    }

    @Test
    void shallPublishStatusUpdateForCertificate() {
        final var certificate = getCertificate();

        final var readyForSignRequestDTO = ReadyForSignRequestDTO.builder().build();
        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(readyForSignRequestDTO).when(csIntegrationRequestFactory).readyForSignRequest();
        doReturn(certificate).when(csIntegrationService).markCertificateReadyForSign(CERTIFICATE_ID, readyForSignRequestDTO);

        readyForSignForCertificateService.readyForSign(CERTIFICATE_ID);

        verify(publishCertificateStatusUpdateService, times(1)).publish(certificate, HandelsekodEnum.KFSIGN);
    }

    @Test
    void shallMonitorLogUtkastMarkedAsSigned() {
        final var certificate = getCertificate();

        final var readyForSignRequestDTO = ReadyForSignRequestDTO.builder().build();
        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(readyForSignRequestDTO).when(csIntegrationRequestFactory).readyForSignRequest();
        doReturn(certificate).when(csIntegrationService).markCertificateReadyForSign(CERTIFICATE_ID, readyForSignRequestDTO);

        readyForSignForCertificateService.readyForSign(CERTIFICATE_ID);

        verify(monitoringLogService, times(1)).logUtkastMarkedAsReadyToSignNotificationSent(CERTIFICATE_ID, TYPE);
    }

    @Test
    void shouldDecorateCertificateFromCSWithInformationFromWC() {
        final var certificate = getCertificate();

        final var readyForSignRequestDTO = ReadyForSignRequestDTO.builder().build();
        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(readyForSignRequestDTO).when(csIntegrationRequestFactory).readyForSignRequest();
        doReturn(certificate).when(csIntegrationService).markCertificateReadyForSign(CERTIFICATE_ID, readyForSignRequestDTO);

        readyForSignForCertificateService.readyForSign(CERTIFICATE_ID);
        verify(decorateCertificateFromCSWithInformationFromWC, times(1)).decorate(certificate);
    }

    private static Certificate getCertificate() {
        final var certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .type(TYPE)
                .build()
        );
        return certificate;
    }
}
