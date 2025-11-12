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
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.facade.RevokeCertificateFacadeService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@Slf4j
@Service("revokeCertificateFromCS")
@RequiredArgsConstructor
public class RevokeCertificateFromCertificateService implements RevokeCertificateFacadeService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final PDLLogService pdlLogService;
    private final MonitoringLogService monitoringLogService;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    private final DecorateCertificateFromCSWithInformationFromWC decorateCertificateFromCSWithInformationFromWC;
    private final PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    private final CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;
    private final HandleMessageNotificationForParentService handleMessageNotificationForParentService;

    @Override
    public Certificate revokeCertificate(String certificateId, String reason, String message) {
        final var exists = csIntegrationService.certificateExists(certificateId);
        if (Boolean.FALSE.equals(exists)) {
            log.debug("Certificate with id '{}' does not exist in certificate service", certificateId);
            return null;
        }

        final var certificate = csIntegrationService.getCertificate(certificateId,
            csIntegrationRequestFactory.getCertificateRequest());

        if (certificate == null) {
            throw new IllegalStateException(
                "Certificate for revocation does not exist in certificate-service");
        }

        final var certificateStatusBeforeRevoke = certificate.getMetadata().getStatus();

        log.debug("Revoking certificate with id '{}' using certificate service", certificateId);
        final var revokedCertificate = csIntegrationService.revokeCertificate(
            certificateId,
            csIntegrationRequestFactory.revokeCertificateRequest(reason, message)
        );

        decorateCertificateFromCSWithInformationFromWC.decorate(revokedCertificate);
        log.debug("Certificate with id '{}' was revoked using certificate service", certificateId);
        pdlLogService.logRevoke(revokedCertificate);

        monitorLog(certificate, revokedCertificate.getMetadata().getRevokedBy().getPersonId(), reason, certificateStatusBeforeRevoke);
        publishCertificateStatusUpdateService.publish(revokedCertificate, HandelsekodEnum.MAKULE);
        publishCertificateAnalyticsMessage.publishEvent(
            certificateAnalyticsMessageFactory.certificateRevoked(revokedCertificate)
        );
        handleMessageNotificationForParentService.notify(revokedCertificate.getMetadata().getRelations());

        return revokedCertificate;
    }

    private void monitorLog(Certificate certificate, String revokedBy, String reason,
        CertificateStatus certificateStatusBeforeRevoke) {
        if (certificateStatusBeforeRevoke == CertificateStatus.LOCKED) {
            monitoringLogService.logUtkastRevoked(
                certificate.getMetadata().getId(),
                revokedBy,
                reason
            );
        }

        if (certificateStatusBeforeRevoke == CertificateStatus.SIGNED) {
            monitoringLogService.logIntygRevoked(
                certificate.getMetadata().getId(),
                certificate.getMetadata().getType(),
                revokedBy,
                reason
            );
        }
    }
}