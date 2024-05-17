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
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.facade.ComplementCertificateFacadeService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Slf4j
@RequiredArgsConstructor
@Service("complementCertificateFromCertificateService")
public class ComplementCertificateFromCertificateService implements ComplementCertificateFacadeService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final WebCertUserService webCertUser;
    private final MonitoringLogService monitoringLogService;
    private final IntegratedUnitRegistryHelper integratedUnitRegistryHelper;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    private final PDLLogService pdlLogService;

    @Override
    public Certificate complement(String certificateId, String message) {
        log.debug("Attempting to complement certificate '{}' from Certificate Service", certificateId);

        if (Boolean.FALSE.equals(csIntegrationService.certificateExists(certificateId))) {
            log.debug("Certificate '{}' does not exist in certificate service", certificateId);
            return null;
        }

        final var certificateToComplement = csIntegrationService.getCertificate(
            certificateId,
            csIntegrationRequestFactory.getCertificateRequest()
        );

        final var certificate = csIntegrationService.complementCertificate(
            certificateId,
            csIntegrationRequestFactory.complementCertificateRequest(
                certificateToComplement.getMetadata().getPatient(),
                webCertUser.getUser().getParameters(),
                message)
        );

        integratedUnitRegistryHelper.addUnitForCopy(certificateToComplement, certificate);

        log.debug("Complemented certificate '{}' from Certificate Service", certificateId);
        monitoringLogService.logIntygCopiedCompletion(
            certificate.getMetadata().getId(),
            certificateToComplement.getMetadata().getId()
        );
        pdlLogService.logCreated(certificate);
        publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.SKAPAT);

        return certificate;
    }

    @Override
    public Certificate answerComplement(String certificateId, String message) {
        return null;
    }
}
