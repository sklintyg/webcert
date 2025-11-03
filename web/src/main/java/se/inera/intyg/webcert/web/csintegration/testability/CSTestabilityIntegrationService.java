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
package se.inera.intyg.webcert.web.csintegration.testability;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceCreateCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateTypeVersionsResponse;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CertificateType;

@Service
public class CSTestabilityIntegrationService {

    private static final String TESTABILITY_CERTIFICATE_ENDPOINT_URL = "/testability/certificate";
    private static final String SUPPORTED_TYPES = "/types";
    private final RestTemplate restTemplate;
    private final IntygModuleRegistry intygModuleRegistry;

    public CSTestabilityIntegrationService(@Qualifier("csRestTemplate") RestTemplate restTemplate,
        IntygModuleRegistry intygModuleRegistry) {
        this.restTemplate = restTemplate;
        this.intygModuleRegistry = intygModuleRegistry;
    }

    @Value("${certificateservice.base.url}")
    private String baseUrl;

    public Certificate createCertificate(CreateCertificateRequestDTO request) {
        final var url = baseUrl + TESTABILITY_CERTIFICATE_ENDPOINT_URL;

        final var response = restTemplate.postForObject(url, request,
            CertificateServiceCreateCertificateResponseDTO.class);

        if (response == null) {
            return null;
        }

        return response.getCertificate();
    }

    public List<CertificateType> getSupportedTypes() {
        final var url = baseUrl + TESTABILITY_CERTIFICATE_ENDPOINT_URL + SUPPORTED_TYPES;

        final var response = restTemplate.getForEntity(url, CertificateType[].class);

        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }

    public List<CertificateModelIdDTO> certificateTypeExists(String certificateType) {
        final var certificateServiceTypeId = getCertificateServiceTypeId(certificateType);
        final var url = baseUrl + TESTABILITY_CERTIFICATE_ENDPOINT_URL + SUPPORTED_TYPES + "/"
            + certificateServiceTypeId;

        final var response = restTemplate.getForEntity(url, GetCertificateTypeVersionsResponse.class);

        if (response.getBody() == null) {
            return List.of();
        }

        return response.getBody().getCertificateModelIds();
    }

    public String getCertificateServiceTypeId(String type) {
        if (intygModuleRegistry.moduleExists(type)) {
            try {
                final var moduleEntryPoint = intygModuleRegistry.getModuleEntryPoint(type);
                return moduleEntryPoint.certificateServiceTypeId();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return type;
    }
}
