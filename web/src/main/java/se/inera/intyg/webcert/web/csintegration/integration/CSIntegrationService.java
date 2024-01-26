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
package se.inera.intyg.webcert.web.csintegration.integration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.webcert.web.csintegration.dto.CertificateServiceTypeInfoDTO;
import se.inera.intyg.webcert.web.csintegration.dto.CertificateServiceTypeInfoRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;

@Service
public class CSIntegrationService {

    private final CertificateTypeInfoConverter certificateTypeInfoConverter;

    private final RestTemplate restTemplate;

    public CSIntegrationService(CertificateTypeInfoConverter certificateTypeInfoConverter, @Qualifier("csRestTemplate")
    RestTemplate restTemplate) {
        this.certificateTypeInfoConverter = certificateTypeInfoConverter;
        this.restTemplate = restTemplate;
    }


    @Value("${certificateservice.base.url}")
    private String baseUrl;

    public List<CertificateTypeInfoDTO> getTypeInfo(CertificateServiceTypeInfoRequestDTO request) {
        final var url = baseUrl + "/api/certificatetypeinfo";
        final var response = restTemplate.postForObject(url, request, CertificateServiceTypeInfoDTO[].class);

        if (response == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(response)
            .map(certificateTypeInfoConverter::convert)
            .collect(Collectors.toList());
    }

}
