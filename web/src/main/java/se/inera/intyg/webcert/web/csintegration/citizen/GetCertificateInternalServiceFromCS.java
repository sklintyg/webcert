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

package se.inera.intyg.webcert.web.csintegration.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.web.controller.internalapi.GetCertificateInteralApi;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetCertificateResponse;

@Slf4j
@RequiredArgsConstructor
@Service("getCertificateInternalServiceFromCS")
public class GetCertificateInternalServiceFromCS implements GetCertificateInteralApi {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;

    @Override
    public GetCertificateResponse get(String certificateId, String personId) {
        final var exists = csIntegrationService.citizenCertificateExists(certificateId);
        if (Boolean.FALSE.equals(exists)) {
            log.debug("Certificate with id '{}' does not exist in certificate service", certificateId);
            return null;
        }

        final var citizenCertificate = csIntegrationService.getCitizenCertificate(
            csIntegrationRequestFactory.getCitizenCertificateRequest(personId),
            certificateId
        );

        return GetCertificateResponse.create(
            citizenCertificate.getCertificate(),
            citizenCertificate.getAvailableFunctions(),
            citizenCertificate.getTexts()
        );
    }
}
