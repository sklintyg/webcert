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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ErrorLogRequestDTO;

@ExtendWith(MockitoExtension.class)
public class ErrorLogFacadeServiceImplTest {

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private ErrorLogFacadeServiceImpl errorLogFacadeService;

    private final static String ERROR_ID = "errorId";
    private final static String CERTIFICATE_ID = "certId";
    private final static String ERROR_MESSAGE = "message";
    private final static String STACK_TRACE = "stack trace";
    private final static String ERROR_CODE = "error code";
    private final static String NO_CERTIFICATE_ID = "NO_CERTIFICATE_ID";
    private final static String NO_STACK_TRACE = "NO_STACK_TRACE";

    @Test
    void shallLogError() {
        var request = createRequest(true, true);

        errorLogFacadeService.log(request);
        verify(monitoringLogService).logClientError(ERROR_ID, CERTIFICATE_ID, ERROR_CODE, ERROR_MESSAGE, STACK_TRACE);
    }

    @Test
    void shallLogErrorWithoutCertificateId() {
        var request = createRequest(false, true);

        errorLogFacadeService.log(request);
        verify(monitoringLogService).logClientError(ERROR_ID, NO_CERTIFICATE_ID, ERROR_CODE, ERROR_MESSAGE, STACK_TRACE);
    }

    @Test
    void shallLogErrorWithoutStackTrace() {
        var request = createRequest(true, false);

        errorLogFacadeService.log(request);
        verify(monitoringLogService).logClientError(ERROR_ID, CERTIFICATE_ID, ERROR_CODE, ERROR_MESSAGE, NO_STACK_TRACE);
    }

    private ErrorLogRequestDTO createRequest(boolean includeId, boolean includeStackTrace) {
        ErrorLogRequestDTO request = new ErrorLogRequestDTO();
        request.setErrorId(ERROR_ID);
        request.setCertificateId(includeId ? CERTIFICATE_ID : NO_CERTIFICATE_ID);
        request.setErrorCode(ERROR_CODE);
        request.setStackTrace(includeStackTrace ? STACK_TRACE : NO_STACK_TRACE);
        request.setMessage(ERROR_MESSAGE);
        return request;
    }
}
