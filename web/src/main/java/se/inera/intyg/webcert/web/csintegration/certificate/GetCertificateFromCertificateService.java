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
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.link.ResourceLinkTypeEnum;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;

@Slf4j
@Service("getCertificateFromCS")
@RequiredArgsConstructor
public class GetCertificateFromCertificateService implements GetCertificateFacadeService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final PDLLogService pdlLogService;
    private final DecorateCertificateFromCSWithInformationFromWC decorateCertificateFromCSWithInformationFromWC;

    @Override
    public Certificate getCertificate(String certificateId, boolean pdlLog, boolean validateAccess) {
        final var exists = csIntegrationService.certificateExists(certificateId);
        if (Boolean.FALSE.equals(exists)) {
            log.debug("Certificate with id '{}' does not exist in certificate service", certificateId);
            return null;
        }

        log.debug("Getting certificate from certificate service with id '{}'", certificateId);
        final var response = csIntegrationService.getCertificate(
            certificateId,
            csIntegrationRequestFactory.getCertificateRequest()
        );
        log.debug("Certificate with id '{}' was retrieved from certificate service", certificateId);

        decorateCertificateFromCSWithInformationFromWC.decorate(response);

        if (pdlLog) {
            pdlLogService.logRead(response);

            if (containsCandidateCertificateInformation(response)) {
                pdlLogService.logReadLevelTwo(response);
            }
        }

        return response;
    }

    private boolean containsCandidateCertificateInformation(Certificate response) {
        return response.getLinks().stream()
            .anyMatch(link -> ResourceLinkTypeEnum.CREATE_CERTIFICATE_FROM_CANDIDATE.equals(link.getType()));
    }
}
