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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.facade.ErrorLogFacadeService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ErrorLogRequestDTO;

@Service
public class ErrorLogFacadeServiceImpl implements ErrorLogFacadeService {

    private final MonitoringLogService monitoringService;

    @Autowired
    public ErrorLogFacadeServiceImpl(MonitoringLogService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @Override
    public void log(ErrorLogRequestDTO request) {
        monitoringService
            .logClientError(request.getErrorId(), getCertificateId(request.getCertificateId()),
                request.getErrorCode(), request.getMessage(), getStackTrace(request.getStackTrace()));
    }

    private boolean isDefined(String value) {
        return value != null && !value.isEmpty() && !value.isBlank();
    }

    private String getCertificateId(String certificateId) {
        return isDefined(certificateId) ? certificateId : "NO_CERTIFICATE_ID";
    }

    private String getStackTrace(String stackTrace) {
        return isDefined(stackTrace) ? stackTrace : "NO_STACK_TRACE";
    }
}
