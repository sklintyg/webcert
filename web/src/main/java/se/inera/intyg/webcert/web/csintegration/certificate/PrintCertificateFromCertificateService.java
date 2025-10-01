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
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrintCertificateFromCertificateService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final PDLLogService pdlLogService;
    private final MonitoringLogService monitoringLogService;
    private final PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    private final CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    public IntygPdf print(String certificateId, String certificateType, boolean isEmployerCopy) {
        final var exists = csIntegrationService.certificateExists(certificateId);
        if (Boolean.FALSE.equals(exists)) {
            log.debug("Certificate '{}' does not exist in certificate service", certificateId);
            return null;
        }

        final var certificate = csIntegrationService.getCertificate(
            certificateId,
            csIntegrationRequestFactory.getCertificateRequest()
        );

        log.debug("Getting pdf of certificate '{}', stored in certificate service", certificateId);
        final var response = csIntegrationService.printCertificate(
            certificateId,
            csIntegrationRequestFactory.getPrintCertificateRequest(
                "Intyget är utskrivet från Webcert.",
                certificate.getMetadata().getPatient().getActualPersonId().getId()
            )
        );

        publishCertificateAnalyticsMessage.publishEvent(
            certificateAnalyticsMessageFactory.print(certificate)
        );

        pdlLogService.logPrinted(certificate);
        logMonitoring(certificate.getMetadata().getStatus(), certificateId, certificateType, isEmployerCopy);

        return response;
    }

    private void logMonitoring(CertificateStatus status, String id, String type, boolean isEmployerCopy) {
        if (status == CertificateStatus.UNSIGNED || status == CertificateStatus.LOCKED) {
            monitoringLogService.logUtkastPrint(id, type);
        }

        if (status == CertificateStatus.REVOKED) {
            monitoringLogService.logRevokedPrint(id, type);
        }

        if (status == CertificateStatus.SIGNED) {
            monitoringLogService.logIntygPrintPdf(id, type, isEmployerCopy);
        }
    }
}
