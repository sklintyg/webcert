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
import se.inera.intyg.webcert.web.service.facade.UpdateCertificateFromCandidateFacadeService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Slf4j
@Service("updateCertificateFromCandidateFromCS")
@RequiredArgsConstructor
public class UpdateCertificateFromCandidateInCertificateService implements UpdateCertificateFromCandidateFacadeService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final WebCertUserService webCertUserService;
    private final PDLLogService pdlLogService;
    private final MonitoringLogService monitoringLogService;
    private final PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    private final PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    private final CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @Override
    public String update(String certificateId) {
        final var exists = csIntegrationService.certificateExists(certificateId);
        if (Boolean.FALSE.equals(exists)) {
            log.debug("Certificate '{}' does not exist in certificate service", certificateId);
            return null;
        }

        final var candidateCertificate = csIntegrationService.getCandidateCertificate(
            certificateId,
            csIntegrationRequestFactory.getCandidateCertificateRequest()
        );

        final var user = webCertUserService.getUser();
        final var savedCertificate = csIntegrationService.updateWithCandidateCertificate(
            certificateId,
            candidateCertificate.getMetadata().getId(),
            csIntegrationRequestFactory.updateWithCandidateCertificateRequestDTO(
                candidateCertificate.getMetadata().getPatient().getActualPersonId().getId(),
                user.getParameters()
            )
        );

        pdlLogService.logRead(candidateCertificate);
        pdlLogService.logSaved(savedCertificate);

        monitoringLogService.logUtkastCreatedTemplateAuto(
            certificateId,
            savedCertificate.getMetadata().getType(),
            user.getHsaId(),
            user.getValdVardenhet().getId(),
            candidateCertificate.getMetadata().getId(),
            candidateCertificate.getMetadata().getType()
        );

        publishCertificateStatusUpdateService.publish(savedCertificate, HandelsekodEnum.ANDRAT);

        publishCertificateAnalyticsMessage.publishEvent(
            certificateAnalyticsMessageFactory.draftUpdatedFromCertificate(savedCertificate)
        );

        return savedCertificate.getMetadata().getId();
    }
}
