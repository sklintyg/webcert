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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
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
}