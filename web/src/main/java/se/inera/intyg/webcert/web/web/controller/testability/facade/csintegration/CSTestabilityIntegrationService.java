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
package se.inera.intyg.webcert.web.web.controller.testability.facade.csintegration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceCreateCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;

@Service
public class CSTestabilityIntegrationService {

    private static final String TESTABILITY_CERTIFICATE_ENDPOINT_URL = "/testability/certificate";
    private final RestTemplate restTemplate;

    public CSTestabilityIntegrationService(@Qualifier("csRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${certificateservice.base.url}")
    private String baseUrl;

    public Certificate createCertificate(CreateCertificateRequestDTO request) {
        final var url = baseUrl + TESTABILITY_CERTIFICATE_ENDPOINT_URL;

        final var response = restTemplate.postForObject(url, request, CertificateServiceCreateCertificateResponseDTO.class);

        if (response == null) {
            return null;
        }

        return response.getCertificate();
    }
}
