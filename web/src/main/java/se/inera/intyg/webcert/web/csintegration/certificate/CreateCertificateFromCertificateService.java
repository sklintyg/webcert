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
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.impl.CreateCertificateException;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@Slf4j
@Service("createCertificateFromCS")
@RequiredArgsConstructor
public class CreateCertificateFromCertificateService implements CreateCertificateFacadeService {

    private static final int NO_PREFILL_ELEMENTS = 0;
    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final PDLLogService pdlLogService;
    private final MonitoringLogService monitoringLogService;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    private final PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    private final CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @Override
    public String create(String certificateType, String patientId) throws CreateCertificateException {
        log.debug("Attempting to create certificate of type '{}'", certificateType);

        final var modelId = csIntegrationService.certificateTypeExists(certificateType);
        if (modelId.isEmpty()) {
            log.debug("Certificate type '{}' does not exist in certificate service", certificateType);
            return null;
        }

        try {
            return createCertificate(patientId, modelId.get());
        } catch (Exception ex) {
            log.error("Failed to create certificate in certificate-service!", ex);
            throw new CreateCertificateException("Could not create certificate in certificate service!");
        }
    }

    private String createCertificate(String patientId, CertificateModelIdDTO modelId) {
        final var certificate = csIntegrationService.createCertificate(
            csIntegrationRequestFactory.createCertificateRequest(modelId, patientId)
        );

        pdlLogService.logCreated(certificate);
        monitoringLogService.logUtkastCreated(
            certificate.getMetadata().getId(),
            certificate.getMetadata().getType(),
            certificate.getMetadata().getUnit().getUnitId(),
            certificate.getMetadata().getIssuedBy().getPersonId(),
            NO_PREFILL_ELEMENTS
        );

        publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.SKAPAT);

        publishCertificateAnalyticsMessage.publishEvent(
            certificateAnalyticsMessageFactory.draftCreated(certificate)
        );

        log.debug("Created certificate using certificate service of type '{}' and version '{}'",
            modelId.getType(),
            modelId.getVersion()
        );

        return certificate.getMetadata().getId();
    }
}
