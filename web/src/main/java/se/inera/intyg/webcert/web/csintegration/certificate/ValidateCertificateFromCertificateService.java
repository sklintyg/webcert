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
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.service.facade.ValidateCertificateFacadeService;

@Slf4j
@Service("validateCertificateFromCS")
@RequiredArgsConstructor
public class ValidateCertificateFromCertificateService implements ValidateCertificateFacadeService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;

    @Override
    public ValidationErrorDTO[] validate(Certificate certificate) {
        final var certificateId = certificate.getMetadata().getId();

        final var exists = csIntegrationService.certificateExists(certificateId);
        if (Boolean.FALSE.equals(exists)) {
            log.debug("Certificate '{}' does not exist in certificate service", certificateId);
            return null;
        }

        log.debug("Validating certificate '{}' stored in certificate service", certificateId);
        final var response = csIntegrationService.validateCertificate(
            csIntegrationRequestFactory.getValidateCertificateRequest(certificate)
        );
        log.debug("Certificate '{}' was validated in certificate service", certificateId);

        return response;
    }
}
