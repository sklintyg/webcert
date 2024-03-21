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

package se.inera.intyg.webcert.web.csintegration.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;

@ExtendWith(MockitoExtension.class)
class PDLLogServiceTest {

    private static final Certificate CERTIFICATE = new Certificate();

    @Mock
    LogService logService;

    @Mock
    LogRequestFactory logRequestFactory;

    @InjectMocks
    PDLLogService pdlLogService;

    @Test
    void shouldLogCreateCertificate() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE);

        pdlLogService.logCreated(CERTIFICATE);

        verify(logService).logCreateIntyg(captor.capture());

        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogReadCertificate() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE);

        pdlLogService.logRead(CERTIFICATE);

        verify(logService).logReadIntyg(captor.capture());

        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogSavedCertificate() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE);

        pdlLogService.logSaved(CERTIFICATE);

        verify(logService).logUpdateIntyg(captor.capture());

        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogDeletedCertificate() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE);

        pdlLogService.logDeleted(CERTIFICATE);

        verify(logService).logDeleteIntyg(captor.capture());

        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Nested
    class Print {

        @Test
        void shouldLogForDraft() {
            final var expectedLogRequest = LogRequest.builder().build();
            final var captor = ArgumentCaptor.forClass(LogRequest.class);
            final var metadata = CertificateMetadata.builder()
                .status(CertificateStatus.UNSIGNED)
                .build();
            CERTIFICATE.setMetadata(metadata);
            doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE);

            pdlLogService.logPrinted(CERTIFICATE);

            verify(logService).logPrintIntygAsDraft(captor.capture());
            assertEquals(expectedLogRequest, captor.getValue());
        }

        @Test
        void shouldLogForLockedDraft() {
            final var expectedLogRequest = LogRequest.builder().build();
            final var captor = ArgumentCaptor.forClass(LogRequest.class);
            final var metadata = CertificateMetadata.builder()
                .status(CertificateStatus.LOCKED)
                .build();
            CERTIFICATE.setMetadata(metadata);
            doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE);

            pdlLogService.logPrinted(CERTIFICATE);

            verify(logService).logPrintIntygAsDraft(captor.capture());
            assertEquals(expectedLogRequest, captor.getValue());
        }

        @Test
        void shouldLogForSigned() {
            final var expectedLogRequest = LogRequest.builder().build();
            final var captor = ArgumentCaptor.forClass(LogRequest.class);
            final var metadata = CertificateMetadata.builder()
                .status(CertificateStatus.SIGNED)
                .build();
            CERTIFICATE.setMetadata(metadata);
            doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE);

            pdlLogService.logPrinted(CERTIFICATE);

            verify(logService).logPrintIntygAsPDF(captor.capture());
            assertEquals(expectedLogRequest, captor.getValue());
        }

        @Test
        void shouldLogForRevoked() {
            final var expectedLogRequest = LogRequest.builder().build();
            final var captor = ArgumentCaptor.forClass(LogRequest.class);
            final var metadata = CertificateMetadata.builder()
                .status(CertificateStatus.REVOKED)
                .build();
            CERTIFICATE.setMetadata(metadata);
            doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE);

            pdlLogService.logPrinted(CERTIFICATE);

            verify(logService).logPrintRevokedIntygAsPDF(captor.capture());
            assertEquals(expectedLogRequest, captor.getValue());
        }
    }

    @Test
    void shouldLogSignCertificate() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE);

        pdlLogService.logSign(CERTIFICATE);

        verify(logService).logSignIntyg(captor.capture());

        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogSentCertificate() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);
        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE);

        pdlLogService.logSent(CERTIFICATE);

        verify(logService).logSendIntygToRecipient(captor.capture());
        assertEquals(expectedLogRequest, captor.getValue());
    }
}
