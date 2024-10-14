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
import se.inera.intyg.webcert.web.service.facade.ReadyForSignFacadeService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@Slf4j
@RequiredArgsConstructor
@Service("readyForSignForCS")
public class ReadyForSignForCertificateService implements ReadyForSignFacadeService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    private final MonitoringLogService monitoringLogService;
    private final DecorateCertificateFromCSWithInformationFromWC decorateCertificateFromCSWithInformationFromWC;

    @Override
    public Certificate readyForSign(String certificateId) {
        log.debug("Attempting to mark certificate '{}' as ready for sign", certificateId);

        if (Boolean.FALSE.equals(csIntegrationService.certificateExists(certificateId))) {
            log.debug("Certificate '{}' does not exist in certificate service", certificateId);
            return null;
        }

        final var certificate = csIntegrationService.markCertificateReadyForSign(
            certificateId,
            csIntegrationRequestFactory.readyForSignRequest()
        );

        decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

        publishCertificateStatusUpdateService.publish(certificate, HandelsekodEnum.KFSIGN);
        monitoringLogService.logUtkastMarkedAsReadyToSignNotificationSent(
            certificateId, certificate.getMetadata().getType()
        );

        return certificate;
    }
}
