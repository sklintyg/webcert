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
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.facade.RenewCertificateFacadeService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

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
        if (certificateToRenew == null) {
            throw new IllegalStateException(
                String.format("Certificate service returned null when getting certificate '%s'", certificateId)
            );
        }

        final var integrationParameters = webCertUserService.getUser().getParameters();
        final var renewalCertificate = csIntegrationService.renewCertificate(
            certificateId,
            csIntegrationRequestFactory.renewCertificateRequest(
                isAlternateSSNDefined(integrationParameters) ? integrationParameters.getAlternateSsn()
                    : certificateToRenew.getMetadata().getPatient().getPersonId().getId(),
                integrationParameters != null ? integrationParameters.getReference() : null
            )
        );

        if (renewalCertificate == null) {
            throw new IllegalStateException("Received null when trying to renew certificate from Certificate Service");
        }

        integratedUnitRegistryHelper.addUnitForCopy(certificateToRenew, renewalCertificate);

        log.debug("Renewed certificate '{}' from Certificate Service", certificateId);
        monitoringLogService.logIntygCopiedRenewal(renewalCertificate.getMetadata().getId(), certificateId);
        pdlLogService.logCreated(renewalCertificate);
        publishCertificateStatusUpdateService.publish(renewalCertificate, HandelsekodEnum.SKAPAT);

        return renewalCertificate.getMetadata().getId();
    }

    private static boolean isAlternateSSNDefined(IntegrationParameters integrationParameters) {
        return integrationParameters != null && integrationParameters.getAlternateSsn() != null && !integrationParameters.getAlternateSsn()
            .isEmpty();
    }
}
