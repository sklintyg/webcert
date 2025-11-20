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
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteDraftsFromCertificateService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final CertificateServiceProfile certificateServiceProfile;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    private final MonitoringLogService monitoringLogService;
    private final HandelseRepository handelseRepository;

    public int delete(LocalDateTime cutoffDate) {
        if (!certificateServiceProfile.active()) {
            return 0;
        }

        final var staleDraftIds = csIntegrationService.listStaleDrafts(
            csIntegrationRequestFactory.getListStaleDraftsRequestDTO(cutoffDate)
        );

        if (staleDraftIds.isEmpty()) {
            return 0;
        }

        return staleDraftIds.stream()
            .mapToInt(certificateId -> deleteSingleDraft(certificateId, cutoffDate))
            .sum();
    }

    private int deleteSingleDraft(String certificateId, LocalDateTime cutoffDate) {
        try {
            final var certificateXml = csIntegrationService.getInternalCertificateXml(certificateId);

            final var deletedCertificate = csIntegrationService.deleteStaleDraft(
                csIntegrationRequestFactory.getDeleteStaleDraftsRequestDTO(certificateId)
            );

            handelseRepository.deleteByIntygsId(certificateId);

            publishCertificateStatusUpdateService.publish(deletedCertificate, HandelsekodEnum.RADERA, certificateXml);

            monitoringLogService.logUtkastPruned(
                deletedCertificate.getMetadata().getId(),
                deletedCertificate.getMetadata().getType(),
                ChronoUnit.DAYS.between(cutoffDate.toLocalDate(), LocalDate.now())
            );

            return 1;
        } catch (Exception e) {
            log.error("Failed to delete stale draft with id: {}", certificateId, e);
            return 0;
        }
    }
}