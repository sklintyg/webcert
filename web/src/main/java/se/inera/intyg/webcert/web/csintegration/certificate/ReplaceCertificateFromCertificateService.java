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
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.facade.ReplaceCertificateFacadeService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Slf4j
@Service("replaceCertificateFromCertificateService")
@RequiredArgsConstructor
public class ReplaceCertificateFromCertificateService implements ReplaceCertificateFacadeService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final PDLLogService pdlLogService;
    private final MonitoringLogService monitoringLogService;
    private final IntegratedUnitRegistryHelper integratedUnitRegistryHelper;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    private final WebCertUserService webCertUserService;
    private final PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    private final CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @Override
    public String replaceCertificate(String certificateId) {
        log.debug("Attempting to replace certificate '{}' from Certificate Service", certificateId);

        if (Boolean.FALSE.equals(csIntegrationService.certificateExists(certificateId))) {
            log.debug("Certificate '{}' does not exist in certificate service", certificateId);
            return null;
        }

        final var certificateToReplace = csIntegrationService.getCertificate(
            certificateId,
            csIntegrationRequestFactory.getCertificateRequest()
        );

        final var replacingCertificate = csIntegrationService.replaceCertificate(
            certificateId,
            csIntegrationRequestFactory.replaceCertificateRequest(
                certificateToReplace.getMetadata().getPatient(),
                webCertUserService.getUser().getParameters()
            )
        );

        integratedUnitRegistryHelper.addUnitForCopy(certificateToReplace, replacingCertificate);

        log.debug("Replaced certificate '{}' from Certificate Service", certificateId);
        monitoringLogService.logIntygCopiedReplacement(replacingCertificate.getMetadata().getId(), certificateId);
        pdlLogService.logCreated(replacingCertificate);
        publishCertificateStatusUpdateService.publish(replacingCertificate, HandelsekodEnum.SKAPAT);
        publishCertificateAnalyticsMessage.publishEvent(
            certificateAnalyticsMessageFactory.certificateReplace(replacingCertificate)
        );

        return replacingCertificate.getMetadata().getId();
    }
}
