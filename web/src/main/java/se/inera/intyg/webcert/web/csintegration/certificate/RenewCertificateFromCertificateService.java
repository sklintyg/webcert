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
import se.inera.intyg.webcert.web.service.facade.RenewCertificateFacadeService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Slf4j
@Service("renewCertificateFromCertificateService")
@RequiredArgsConstructor
public class RenewCertificateFromCertificateService implements RenewCertificateFacadeService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final PDLLogService pdlLogService;
    private final MonitoringLogService monitoringLogService;
    private final WebCertUserService webCertUserService;
    private final IntegratedUnitRegistryHelper integratedUnitRegistryHelper;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    private final PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    private final CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @Override
    public String renewCertificate(String certificateId) {
        log.debug("Attempting to renew certificate '{}' from Certificate Service", certificateId);

        if (Boolean.FALSE.equals(csIntegrationService.certificateExists(certificateId))) {
            log.debug("Certificate '{}' does not exist in certificate service", certificateId);
            return null;
        }

        final var certificateToRenew = csIntegrationService.getCertificate(
            certificateId,
            csIntegrationRequestFactory.getCertificateRequest()
        );

        final var renewalCertificate = csIntegrationService.renewCertificate(
            certificateId,
            csIntegrationRequestFactory.renewCertificateRequest(
                certificateToRenew.getMetadata().getPatient(),
                webCertUserService.getUser().getParameters()
            )
        );

        integratedUnitRegistryHelper.addUnitForCopy(certificateToRenew, renewalCertificate);

        log.debug("Renewed certificate '{}' from Certificate Service", certificateId);
        monitoringLogService.logIntygCopiedRenewal(renewalCertificate.getMetadata().getId(), certificateId);
        pdlLogService.logCreated(renewalCertificate);
        publishCertificateStatusUpdateService.publish(renewalCertificate, HandelsekodEnum.SKAPAT);
        publishCertificateAnalyticsMessage.publishEvent(
            certificateAnalyticsMessageFactory.certificateRenewed(renewalCertificate)
        );

        return renewalCertificate.getMetadata().getId();
    }
}
