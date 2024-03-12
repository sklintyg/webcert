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
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceCreateCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceGetCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateTypeExistsResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateXmlRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateXmlResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetListCertificatesResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetPatientCertificatesRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesInfoResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SignCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SignCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ValidateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ValidateCertificateResponseDTO;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.StaffListInfo;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;

@Service
public class CSIntegrationService {

    private static final String CERTIFICATE_ENDPOINT_URL = "/api/certificate";
    private static final String PATIENT_ENDPOINT_URL = "/api/patient";
    private static final String CERTIFICATE_TYPE_INFO_ENDPOINT_URL = "/api/certificatetypeinfo";
    private static final String UNIT_ENDPOINT_URL = "/api/unit";

    private final CertificateTypeInfoConverter certificateTypeInfoConverter;
    private final ListIntygEntryConverter listIntygEntryConverter;

    private final RestTemplate restTemplate;

    @Value("${certificateservice.base.url}")
    private String baseUrl;

    public CSIntegrationService(CertificateTypeInfoConverter certificateTypeInfoConverter, ListIntygEntryConverter listIntygEntryConverter,
        @Qualifier("csRestTemplate")
        RestTemplate restTemplate) {
        this.certificateTypeInfoConverter = certificateTypeInfoConverter;
        this.listIntygEntryConverter = listIntygEntryConverter;
        this.restTemplate = restTemplate;
    }

    public List<ListIntygEntry> listCertificatesForUnit(GetUnitCertificatesRequestDTO request) {
        final var url = baseUrl + UNIT_ENDPOINT_URL + "/certificates";
        final var response = restTemplate.postForObject(url, request, GetListCertificatesResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException("Response from certificate service was null when getting unit certificate list");
        }

        return response.getCertificates()
            .stream()
            .map(listIntygEntryConverter::convert)
            .collect(Collectors.toList());
    }

    public List<StaffListInfo> listCertificatesInfoForUnit(GetUnitCertificatesInfoRequestDTO request) {
        final var url = baseUrl + UNIT_ENDPOINT_URL + "/certificates/info";
        final var response = restTemplate.postForObject(url, request, GetUnitCertificatesInfoResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException("Response from certificate service was null when getting certificate list info");
        }

        return response.getStaffs()
            .stream()
            .map(staff ->
                StaffListInfo.builder()
                    .hsaId(staff.getPersonId())
                    .name(staff.getFullName())
                    .build()
            )
            .collect(Collectors.toList());
    }

    public List<ListIntygEntry> listCertificatesForPatient(GetPatientCertificatesRequestDTO request) {
        final var url = baseUrl + PATIENT_ENDPOINT_URL + "/certificates";
        final var response = restTemplate.postForObject(url, request, GetListCertificatesResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException("Response from certificate service was null when getting patient certificate list");
        }

        return response.getCertificates()
            .stream()
            .map(listIntygEntryConverter::convert)
            .collect(Collectors.toList());
    }

    public Certificate deleteCertificate(String certificateId, long version, DeleteCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/" + version;
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final var response = restTemplate.<DeleteCertificateResponseDTO>exchange(
            url,
            HttpMethod.DELETE,
            new HttpEntity<>(request, headers),
            new ParameterizedTypeReference<>() {
            }
        );

        if (response.getBody() == null) {
            throw new IllegalStateException(
                String.format("Deleting certificate '%s' returned empty response!", certificateId)
            );
        }

        return response.getBody().getCertificate();
    }

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

        final var response = restTemplate.postForObject(url, request, CertificateServiceCreateCertificateResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException("Certificate service returned null response!");
        }

        return response.getCertificate();
    }

    public Certificate getCertificate(String certificateId, GetCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId;

        final var response = restTemplate.postForObject(url, request, CertificateServiceGetCertificateResponseDTO.class);

        if (response == null) {
            return null;
        }

        return response.getCertificate();
    }

    public ValidationErrorDTO[] validateCertificate(ValidateCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + request.getCertificate().getMetadata().getId() + "/validate";

        final var response = restTemplate.postForObject(url, request, ValidateCertificateResponseDTO.class);

        if (response == null) {
            return null;
        }

        return response.getValidationErrors();
    }

    public Optional<CertificateModelIdDTO> certificateTypeExists(String certificateType) {
        final var url = baseUrl + CERTIFICATE_TYPE_INFO_ENDPOINT_URL + "/" + certificateType + "/exists";
        final var response = restTemplate.getForObject(url, CertificateTypeExistsResponseDTO.class);

        if (response == null
            || response.getCertificateModelId() == null
            || response.getCertificateModelId().getType() == null
            || response.getCertificateModelId().getVersion() == null) {
            return Optional.empty();
        }

        return Optional.of(response.getCertificateModelId());
    }

    public Boolean certificateExists(String certificateId) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/exists";

        final var response = restTemplate.getForObject(url, CertificateExistsResponseDTO.class);

        if (response == null) {
            return false;
        }

        return Boolean.TRUE.equals(response.getExists());
    }

    public Certificate saveCertificate(SaveCertificateRequestDTO request) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + request.getCertificate().getMetadata().getId();
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final var response = restTemplate.<SaveCertificateResponseDTO>exchange(
            url,
            HttpMethod.PUT,
            new HttpEntity<>(request, headers),
            new ParameterizedTypeReference<>() {
            },
            Collections.emptyMap()
        );
        if (response.getBody() == null) {
            throw new IllegalStateException(
                String.format("Saving certificate '%s' returned empty response!",
                    request.getCertificate().getMetadata().getId()
                )
            );
        }
        return response.getBody().getCertificate();
    }

    public GetCertificateXmlResponseDTO getCertificateXml(GetCertificateXmlRequestDTO request, String certificateId) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/xml";

        return restTemplate.postForObject(url, request, GetCertificateXmlResponseDTO.class);
    }

    public String signCertificate(SignCertificateRequestDTO request, String certificateId) {
        final var url = baseUrl + CERTIFICATE_ENDPOINT_URL + "/" + certificateId + "/sign";

        final var response = restTemplate.postForObject(url, request, SignCertificateResponseDTO.class);

        if (response == null) {
            return null;
        }

        return response.getCertificateId();
    }
}
