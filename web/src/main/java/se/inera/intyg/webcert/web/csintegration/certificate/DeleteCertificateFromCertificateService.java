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
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.facade.DeleteCertificateFacadeService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@Slf4j
@Service("deleteCertificateFromCertificateService")
@RequiredArgsConstructor
public class DeleteCertificateFromCertificateService implements DeleteCertificateFacadeService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final PDLLogService pdlLogService;
    private final MonitoringLogService monitoringLogService;

    @Override
    public boolean deleteCertificate(String certificateId, long version) {
        log.debug("Attempting to delete certificate '{}' with version '{}' from Certificate Service", certificateId, version);

        if (Boolean.FALSE.equals(csIntegrationService.certificateExists(certificateId))) {
            return false;
        }

        final var certificate = csIntegrationService.deleteCertificate(
            certificateId, version, csIntegrationRequestFactory.deleteCertificateRequest()
        );

        if (certificate == null) {
            throw new IllegalStateException("Received null when trying to delete certificate from Certificate Service");
        }

        log.debug("Deleted certificate '{}' from Certificate Service", certificateId);
        monitoringLogService.logUtkastDeleted(certificate.getMetadata().getId(), certificate.getMetadata().getType());
        pdlLogService.logDeleted(certificate);

        return true;
    }
}
