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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.facade.SaveCertificateFacadeService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@Slf4j
@Service("saveCertificateFacadeServiceCS")
@RequiredArgsConstructor
public class SaveCertificateInCertificateService implements SaveCertificateFacadeService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final PDLLogService pdlLogService;
    private final MonitoringLogService monitoringLogService;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    private final PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    private final CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @Override
    public long saveCertificate(Certificate certificate, boolean pdlLog) {
        log.debug("Attempting to save certificate '{}' of type '{}'",
            certificate.getMetadata().getId(),
            certificate.getMetadata().getType()
        );

        final var exists = csIntegrationService.certificateExists(certificate.getMetadata().getId());
        if (Boolean.FALSE.equals(exists)) {
            log.debug("Certificate '{}' does not exist in certificate service", certificate.getMetadata().getId());
            return -1;
        }

        final var savedCertificate = csIntegrationService.saveCertificate(
            csIntegrationRequestFactory.saveRequest(
                certificate,
                certificate.getMetadata().getPatient().getActualPersonId().getId()
            )
        );

        if (pdlLog) {
            pdlLogService.logSaved(savedCertificate);
            monitoringLogService.logUtkastEdited(
                certificate.getMetadata().getId(),
                certificate.getMetadata().getType()
            );
        }

        publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.ANDRAT);
        publishCertificateAnalyticsMessage.publishEvent(
            certificateAnalyticsMessageFactory.draftUpdated(savedCertificate)
        );

        return savedCertificate.getMetadata().getVersion();
    }
}
