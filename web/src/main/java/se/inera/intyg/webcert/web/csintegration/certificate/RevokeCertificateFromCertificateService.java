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

package se.inera.intyg.webcert.web.csintegration.certificate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
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

    @Override
    public Certificate revokeCertificate(String certificateId, String reason, String message) {
        final var exists = csIntegrationService.certificateExists(certificateId);
        if (Boolean.FALSE.equals(exists)) {
            log.debug("Certificate with id '{}' does not exist in certificate service", certificateId);
            return null;
        }

        log.debug("Revoking certificate with id '{}' using certificate service", certificateId);
        final var response = csIntegrationService.revokeCertificate(
            certificateId,
            csIntegrationRequestFactory.revokeCertificateRequest(reason, message)
        );

        if (response == null) {
            throw new IllegalStateException("Received null when trying to delete certificate from Certificate Service");
        }

        log.debug("Certificate with id '{}' was revoked using certificate service", certificateId);
        pdlLogService.logRevoke(response);
        monitorLog(response, reason, message);

        return response;
    }

    private void monitorLog(Certificate certificate, String reason, String message) {
        if (certificate.getMetadata().getStatus() == CertificateStatus.LOCKED) {
            monitoringLogService.logUtkastRevoked(
                certificate.getMetadata().getId(),
                certificate.getMetadata().getType(),
                reason,
                message
            );
        }

        if (certificate.getMetadata().getStatus() == CertificateStatus.SIGNED) {
            monitoringLogService.logIntygRevoked(
                certificate.getMetadata().getId(),
                certificate.getMetadata().getType(),
                reason,
                message
            );
        }
    }
}
