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

import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Service
public class PDLLogService {

    private final LogRequestFactory logRequestFactory;
    private final LogService logService;
    private final WebCertUserService webCertUserService;

    public PDLLogService(LogRequestFactory logRequestFactory, LogService logService, WebCertUserService webCertUserService) {
        this.logRequestFactory = logRequestFactory;
        this.logService = logService;
        this.webCertUserService = webCertUserService;
    }

    public void logCreated(String patientId, String certificateId) {
        final var request = logRequestFactory.createLogRequestFromUser(webCertUserService.getUser(), patientId);
        request.setIntygId(certificateId);

        logService.logCreateIntyg(request);
    }

    public void logRead(String patientId, String certificateId) {
        final var request = logRequestFactory.createLogRequestFromUser(webCertUserService.getUser(), patientId);
        request.setIntygId(certificateId);

        logService.logReadIntyg(request);
    }
}
