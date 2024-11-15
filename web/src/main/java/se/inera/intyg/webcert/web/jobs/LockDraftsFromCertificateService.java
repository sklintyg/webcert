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

package se.inera.intyg.webcert.web.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@Service
@RequiredArgsConstructor
public class LockDraftsFromCertificateService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final CertificateServiceProfile certificateServiceProfile;
    private final MonitoringLogService monitoringService;

    @PerformanceLogging(eventAction = "job-lock-drafts-from-certificate-service", eventType = MdcLogConstants.EVENT_TYPE_CHANGE,
        eventCategory = MdcLogConstants.EVENT_CATEGORY_PROCESS)
    public int lock(int lockedAfterDay) {
        if (!certificateServiceProfile.active()) {
            return 0;
        }

        final var certificates = csIntegrationService.lockDrafts(
            csIntegrationRequestFactory.getLockDraftsRequestDTO(lockedAfterDay)
        );

        certificates.forEach(certificate ->
            monitoringService.logUtkastLocked(certificate.getMetadata().getId(), certificate.getMetadata().getType())
        );

        return certificates.size();
    }
}
