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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.certificate.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateTypeExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;

@Service
public class CSIntegrationService {

    private static final String CERTIFICATE_ENDPOINT_URL = "/api/certificate";
    private static final String CERTIFICATE_TYPE_INFO_ENDPOINT_URL = "/api/certificatetypeinfo";
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
        final var url = baseUrl + CERTIFICATE_TYPE_INFO_ENDPOINT_URL;
        final var response = restTemplate.postForObject(url, request, CertificateServiceTypeInfoResponseDTO.class);

        if (response == null) {
            return Collections.emptyList();
        }

        return response.getList()
            .stream()
            .map(certificateTypeInfoConverter::convert)
            .collect(Collectors.toList());
    }

    public Certificate createCertificate(CreateCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL;

        return restTemplate.postForObject(url, request, Certificate.class);
    }

    public Certificate getCertificate(String certificateId, GetCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId;

        return restTemplate.postForObject(url, request, Certificate.class);
    }

    public CertificateModelIdDTO certificateTypeExists(String certificateType) {
        final var url = baseUrl + CERTIFICATE_TYPE_INFO_ENDPOINT_URL + "/" + certificateType + "/exists";
        final var response = restTemplate.getForObject(url, CertificateTypeExistsResponseDTO.class);

        if (response == null) {
            return null;
        }

        return response.getId();
    }

    public Boolean certificateExists(String certificateId) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/exists";

        final var response = restTemplate.getForObject(url, CertificateExistsResponseDTO.class);

        if (response == null) {
            return false;
        }

        return Boolean.TRUE.equals(response.getExists());
    }
}
