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
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFromTemplateFacadeService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogServiceImpl;

@Slf4j
@Service("createCertificateFromTemplateFromCS")
@RequiredArgsConstructor
public class CreateCertificateFromTemplateFromCertificateService implements CreateCertificateFromTemplateFacadeService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final MonitoringLogServiceImpl monitoringLogService;
    private final PDLLogService pdlLogService;
    private final PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    private final CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;

  @Override
    public String createCertificateFromTemplate(String certificateId) {
        final var existsInCS = csIntegrationService.certificateExists(certificateId);
        if (!existsInCS) {
            log.debug("Certificate '{}' does not exist in Certificate Service", certificateId);
            return null;
        }

        final var originalCertificate = csIntegrationService.getCertificate(
            certificateId,
            csIntegrationRequestFactory.getCertificateRequest()
        );

        log.debug("Creating certificate from template with id '{}' from Certificate Service", certificateId);
        final var createCertificateFromTemplateRequest = csIntegrationRequestFactory
            .createCertificateFromTemplateRequest();

        final var response = csIntegrationService.createCertificateFromTemplate(certificateId, createCertificateFromTemplateRequest);

        if (response == null || response.getCertificate() == null) {
            log.debug("Certificate Service returned null when creating certificate from template with id '{}'", certificateId);
            return null;
        }

        final var createdCertificate = response.getCertificate();
        pdlLogService.logCreated(createdCertificate);
        publishCertificateAnalyticsMessage.publishEvent(
            certificateAnalyticsMessageFactory.draftCreatedFromCertificate(createdCertificate)
        );
        monitoringLogService.logUtkastCreatedTemplateManual(
            createdCertificate.getMetadata().getId(),
            createdCertificate.getMetadata().getType(),
            createdCertificate.getMetadata().getIssuedBy().getPersonId(),
            createdCertificate.getMetadata().getUnit().getUnitId(),
            certificateId,
            originalCertificate.getMetadata().getType()
        );
        publishCertificateStatusUpdateService.publish(createdCertificate, HandelsekodEnum.SKAPAT);

        log.debug("Successfully created certificate from template with new id '{}' in Certificate Service",
            response.getCertificate().getMetadata().getId());
        return response.getCertificate().getMetadata().getId();
    }
}
