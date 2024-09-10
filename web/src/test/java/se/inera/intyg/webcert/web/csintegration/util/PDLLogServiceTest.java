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
import static org.mockito.Mockito.mock;
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
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
class PDLLogServiceTest {

    private static final Certificate CERTIFICATE = new Certificate();
    private static final String PERSON_ID = "personId";
    private static final String CERTIFICATE_ID = "certificateId";

    @Mock
    WebCertUserService webCertUserService;

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

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, false);

        pdlLogService.logCreated(CERTIFICATE);

        verify(logService).logCreateIntyg(captor.capture());

        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogCreateCertificateWithSjf() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);
        final var mockedUser = mock(WebCertUser.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, true);
        doReturn(mockedUser).when(webCertUserService).getUser();
        doReturn(sjfIntegrationParameter()).when(mockedUser).getParameters();

        pdlLogService.logCreated(CERTIFICATE);

        verify(logService).logCreateIntyg(captor.capture());

        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogReadCertificate() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, false);

        pdlLogService.logRead(CERTIFICATE);

        verify(logService).logReadIntyg(captor.capture());

        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogReadCertificateWithSjf() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);

        final var mockedUser = mock(WebCertUser.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, true);
        doReturn(mockedUser).when(webCertUserService).getUser();
        doReturn(sjfIntegrationParameter()).when(mockedUser).getParameters();

        pdlLogService.logRead(CERTIFICATE);

        verify(logService).logReadIntyg(captor.capture());

        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogSavedCertificate() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, false);

        pdlLogService.logSaved(CERTIFICATE);

        verify(logService).logUpdateIntyg(captor.capture());

        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogSavedCertificateWithSjf() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);
        final var mockedUser = mock(WebCertUser.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, true);
        doReturn(mockedUser).when(webCertUserService).getUser();
        doReturn(sjfIntegrationParameter()).when(mockedUser).getParameters();

        pdlLogService.logSaved(CERTIFICATE);

        verify(logService).logUpdateIntyg(captor.capture());

        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogDeletedCertificate() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, false);

        pdlLogService.logDeleted(CERTIFICATE);

        verify(logService).logDeleteIntyg(captor.capture());

        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogDeletedCertificateWithSjf() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);
        final var mockedUser = mock(WebCertUser.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, true);
        doReturn(mockedUser).when(webCertUserService).getUser();
        doReturn(sjfIntegrationParameter()).when(mockedUser).getParameters();

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
            doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, false);

            pdlLogService.logPrinted(CERTIFICATE);

            verify(logService).logPrintIntygAsDraft(captor.capture());
            assertEquals(expectedLogRequest, captor.getValue());
        }

        @Test
        void shouldLogForDraftWithSjf() {
            final var expectedLogRequest = LogRequest.builder().build();
            final var captor = ArgumentCaptor.forClass(LogRequest.class);
            final var mockedUser = mock(WebCertUser.class);
            final var metadata = CertificateMetadata.builder()
                .status(CertificateStatus.UNSIGNED)
                .build();
            CERTIFICATE.setMetadata(metadata);

            doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, true);
            doReturn(mockedUser).when(webCertUserService).getUser();
            doReturn(sjfIntegrationParameter()).when(mockedUser).getParameters();

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
            doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, false);

            pdlLogService.logPrinted(CERTIFICATE);

            verify(logService).logPrintIntygAsDraft(captor.capture());
            assertEquals(expectedLogRequest, captor.getValue());
        }

        @Test
        void shouldLogForLockedDraftWithSjf() {
            final var expectedLogRequest = LogRequest.builder().build();
            final var captor = ArgumentCaptor.forClass(LogRequest.class);
            final var mockedUser = mock(WebCertUser.class);
            final var metadata = CertificateMetadata.builder()
                .status(CertificateStatus.LOCKED)
                .build();
            CERTIFICATE.setMetadata(metadata);

            doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, true);
            doReturn(mockedUser).when(webCertUserService).getUser();
            doReturn(sjfIntegrationParameter()).when(mockedUser).getParameters();

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
            doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, false);

            pdlLogService.logPrinted(CERTIFICATE);

            verify(logService).logPrintIntygAsPDF(captor.capture());
            assertEquals(expectedLogRequest, captor.getValue());
        }

        @Test
        void shouldLogForSignedWithSjf() {
            final var expectedLogRequest = LogRequest.builder().build();
            final var captor = ArgumentCaptor.forClass(LogRequest.class);
            final var mockedUser = mock(WebCertUser.class);
            final var metadata = CertificateMetadata.builder()
                .status(CertificateStatus.SIGNED)
                .build();
            CERTIFICATE.setMetadata(metadata);

            doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, true);
            doReturn(mockedUser).when(webCertUserService).getUser();
            doReturn(sjfIntegrationParameter()).when(mockedUser).getParameters();

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
            doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, false);

            pdlLogService.logPrinted(CERTIFICATE);

            verify(logService).logPrintRevokedIntygAsPDF(captor.capture());
            assertEquals(expectedLogRequest, captor.getValue());
        }

        @Test
        void shouldLogForRevokedWithSjf() {
            final var expectedLogRequest = LogRequest.builder().build();
            final var captor = ArgumentCaptor.forClass(LogRequest.class);
            final var mockedUser = mock(WebCertUser.class);
            final var metadata = CertificateMetadata.builder()
                .status(CertificateStatus.REVOKED)
                .build();
            CERTIFICATE.setMetadata(metadata);

            doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, true);
            doReturn(mockedUser).when(webCertUserService).getUser();
            doReturn(sjfIntegrationParameter()).when(mockedUser).getParameters();

            pdlLogService.logPrinted(CERTIFICATE);

            verify(logService).logPrintRevokedIntygAsPDF(captor.capture());
            assertEquals(expectedLogRequest, captor.getValue());
        }
    }

    @Test
    void shouldLogSignCertificate() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, false);

        pdlLogService.logSign(CERTIFICATE);

        verify(logService).logSignIntyg(captor.capture());

        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogSignCertificateWithSjf() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);
        final var mockedUser = mock(WebCertUser.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, true);
        doReturn(mockedUser).when(webCertUserService).getUser();
        doReturn(sjfIntegrationParameter()).when(mockedUser).getParameters();

        pdlLogService.logSign(CERTIFICATE);

        verify(logService).logSignIntyg(captor.capture());

        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogSentCertificate() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);
        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, false);

        pdlLogService.logSent(CERTIFICATE);

        verify(logService).logSendIntygToRecipient(captor.capture());
        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogSentCertificateWithSjf() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);
        final var mockedUser = mock(WebCertUser.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, true);
        doReturn(mockedUser).when(webCertUserService).getUser();
        doReturn(sjfIntegrationParameter()).when(mockedUser).getParameters();

        pdlLogService.logSent(CERTIFICATE);

        verify(logService).logSendIntygToRecipient(captor.capture());
        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogRevokeCertificate() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);
        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, false);

        pdlLogService.logRevoke(CERTIFICATE);

        verify(logService).logRevokeIntyg(captor.capture());
        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogRevokeCertificateWithSjf() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var captor = ArgumentCaptor.forClass(LogRequest.class);
        final var mockedUser = mock(WebCertUser.class);

        doReturn(expectedLogRequest).when(logRequestFactory).createLogRequestFromCertificate(CERTIFICATE, true);
        doReturn(mockedUser).when(webCertUserService).getUser();
        doReturn(sjfIntegrationParameter()).when(mockedUser).getParameters();

        pdlLogService.logRevoke(CERTIFICATE);

        verify(logService).logRevokeIntyg(captor.capture());
        assertEquals(expectedLogRequest, captor.getValue());
    }

    @Test
    void shouldLogCreateMessage() {
        final var webCertUser = new WebCertUser();
        doReturn(webCertUser).when(webCertUserService).getUser();

        pdlLogService.logCreateMessage(
            PERSON_ID, CERTIFICATE_ID
        );

        verify(logService).logCreateMessage(webCertUser, PERSON_ID, CERTIFICATE_ID);
    }

    private IntegrationParameters sjfIntegrationParameter() {
        return IntegrationParameters.of(null, null, null, null, null, null, null, null, null, true, false, false, false);
    }
}
