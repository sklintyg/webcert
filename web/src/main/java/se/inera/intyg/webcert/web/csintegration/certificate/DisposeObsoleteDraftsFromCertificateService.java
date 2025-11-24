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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisposeObsoleteDraftsFromCertificateService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final CertificateServiceProfile certificateServiceProfile;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    private final MonitoringLogService monitoringLogService;
    private final PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    private final CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    public int dispose(LocalDateTime disposeObsoleteDraftsDate) {
        if (!certificateServiceProfile.active()) {
            return 0;
        }

        final var obsoleteDraftIds = csIntegrationService.listObsoleteDrafts(
            csIntegrationRequestFactory.getListObsoleteDraftsRequestDTO(disposeObsoleteDraftsDate)
        );

        if (obsoleteDraftIds.isEmpty()) {
            return 0;
        }

        return obsoleteDraftIds.stream()
            .mapToInt(certificateId -> disposeDraft(certificateId, disposeObsoleteDraftsDate))
            .sum();
    }

    private int disposeDraft(String certificateId, LocalDateTime disposeObsoleteDraftsDate) {
        try {
            final var certificateXml = csIntegrationService.getInternalCertificateXml(certificateId);

            final var disposedCertificate = csIntegrationService.disposeObsoleteDraft(
                csIntegrationRequestFactory.getDisposeObsoleteDraftRequestDTO(certificateId)
            );

            publishCertificateStatusUpdateService.publish(disposedCertificate, HandelsekodEnum.RADERA, certificateXml);

            monitoringLogService.logUtkastDisposed(
                disposedCertificate.getMetadata().getId(),
                disposedCertificate.getMetadata().getType(),
                ChronoUnit.DAYS.between(disposeObsoleteDraftsDate.toLocalDate(), LocalDate.now())
            );

            publishCertificateAnalyticsMessage.publishEvent(certificateAnalyticsMessageFactory.draftDisposed(disposedCertificate));
            return 1;
        } catch (Exception e) {
            log.error("Failed to dispose obsolete draft with id: {}", certificateId, e);
            return 0;
        }
    }
}