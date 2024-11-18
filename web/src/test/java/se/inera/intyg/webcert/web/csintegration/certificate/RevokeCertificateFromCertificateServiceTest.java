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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
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
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@ExtendWith(MockitoExtension.class)
class RevokeCertificateFromCertificateServiceTest {

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
    DecorateCertificateFromCSWithInformationFromWC decorateCertificateFromCSWithInformationFromWC;

    @InjectMocks
    RevokeCertificateFromCertificateService revokeCertificateFromCertificateService;

    private static final String ID = "ID";
    private static final String TYPE = "TYPE";
    private static final String REASON = "REASON";
    private static final String MESSAGE = "MESSAGE";
    private static final Certificate CERTIFICATE_AFTER_REVOKE = new Certificate();
    private static final Certificate CERTIFICATE_BEFORE_REVOKE = new Certificate();
    private static final GetCertificateRequestDTO GET_REQUEST = GetCertificateRequestDTO.builder().build();
    private static final RevokeCertificateRequestDTO REVOKE_REQUEST = RevokeCertificateRequestDTO.builder().build();

    @Test
    void shouldReturnNullIfCertificateDoesNotExistInCS() {
        final var response = revokeCertificateFromCertificateService.revokeCertificate(ID, REASON, MESSAGE);

        assertNull(response);
    }

    @Nested
    class CertificateExistsInCS {

        @BeforeEach
        void setup() {

            CERTIFICATE_BEFORE_REVOKE.setMetadata(CertificateMetadata.builder()
                .id(ID)
                .type(TYPE)
                .status(CertificateStatus.SIGNED)
                .build());

            CERTIFICATE_AFTER_REVOKE.setMetadata(CertificateMetadata.builder()
                .id(ID)
                .type(TYPE)
                .status(CertificateStatus.LOCKED)
                .build());

            when(csIntegrationService.certificateExists(ID))
                .thenReturn(true);

            when(csIntegrationRequestFactory.getCertificateRequest()).thenReturn(GET_REQUEST);
        }

        @Nested
        class CertificateIsRevokedFromCS {

            @BeforeEach
            void setup() {
                when(csIntegrationService.getCertificate(ID, GET_REQUEST)).thenReturn(CERTIFICATE_BEFORE_REVOKE);
                when(csIntegrationService.revokeCertificate(ID, REVOKE_REQUEST)).thenReturn(CERTIFICATE_AFTER_REVOKE);
                when(csIntegrationRequestFactory.revokeCertificateRequest(REASON, MESSAGE)).thenReturn(REVOKE_REQUEST);
            }

            @Test
            void shouldCallRevokeWithId() {
                final var captor = ArgumentCaptor.forClass(String.class);
                revokeCertificateFromCertificateService.revokeCertificate(ID, REASON, MESSAGE);

                verify(csIntegrationService).revokeCertificate(captor.capture(), any(RevokeCertificateRequestDTO.class));
                assertEquals(ID, captor.getValue());
            }

            @Test
            void shouldCallRevokeWithRequest() {
                final var captor = ArgumentCaptor.forClass(RevokeCertificateRequestDTO.class);
                revokeCertificateFromCertificateService.revokeCertificate(ID, REASON, MESSAGE);

                verify(csIntegrationService).revokeCertificate(anyString(), captor.capture());
                assertEquals(REVOKE_REQUEST, captor.getValue());
            }

            @Test
            void shouldPdlLogRevoke() {
                revokeCertificateFromCertificateService.revokeCertificate(ID, REASON, MESSAGE);
                verify(pdlLogService).logRevoke(CERTIFICATE_AFTER_REVOKE);

            }

            @Test
            void shouldMonitorLogRevokeLockedDraft() {
                CERTIFICATE_BEFORE_REVOKE.getMetadata().setStatus(CertificateStatus.LOCKED);
                revokeCertificateFromCertificateService.revokeCertificate(ID, REASON, MESSAGE);
                verify(monitoringLogService).logUtkastRevoked(ID, TYPE, REASON, MESSAGE);
            }

            @Test
            void shouldMonitorLogRevokeCertificate() {
                revokeCertificateFromCertificateService.revokeCertificate(ID, REASON, MESSAGE);
                verify(monitoringLogService).logIntygRevoked(ID, TYPE, REASON, MESSAGE);
            }

            @Test
            void shouldPublishCertificateStatusUpdate() {
                revokeCertificateFromCertificateService.revokeCertificate(ID, REASON, MESSAGE);
                verify(publishCertificateStatusUpdateService).publish(CERTIFICATE_AFTER_REVOKE, HandelsekodEnum.MAKULE);
            }

            @Test
            void shouldDecorateCertificateFromCSWithInformationFromWC() {
                revokeCertificateFromCertificateService.revokeCertificate(ID, REASON, MESSAGE);
                verify(decorateCertificateFromCSWithInformationFromWC, times(1)).decorate(CERTIFICATE_AFTER_REVOKE);
            }
        }

        @Test
        void shouldThrowExceptionIfReturnedCertificateIsNull() {
            assertThrows(IllegalStateException.class, () -> revokeCertificateFromCertificateService.revokeCertificate(ID, REASON, MESSAGE));
        }
    }
}
