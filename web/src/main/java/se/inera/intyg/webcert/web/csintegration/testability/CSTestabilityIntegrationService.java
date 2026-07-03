/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.webcert.logging.MdcLogConstants.SESSION_ID_KEY;
import static se.inera.intyg.webcert.logging.MdcLogConstants.TRACE_ID_KEY;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceCreateCertificateResponseDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateTypeVersionsResponse;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CertificateType;

@Service
public class CSTestabilityIntegrationService {

  private static final String TESTABILITY_CERTIFICATE_ENDPOINT_URL = "/testability/certificate";
  private static final String SUPPORTED_TYPES = "/types";
  private final RestClient restClient;
  private final IntygModuleRegistry intygModuleRegistry;

  public CSTestabilityIntegrationService(
      @Qualifier("csRestClient") RestClient restClient, IntygModuleRegistry intygModuleRegistry) {
    this.restClient = restClient;
    this.intygModuleRegistry = intygModuleRegistry;
  }

  @Value("${certificateservice.base.url}")
  private String baseUrl;

  public Certificate createCertificate(CreateCertificateRequestDTO request) {
    final var url = baseUrl + TESTABILITY_CERTIFICATE_ENDPOINT_URL;

    final var convertedRequest = convertCertificateType(request);

    final var response =
        restClient
            .post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(convertedRequest)
            .retrieve()
            .body(CertificateServiceCreateCertificateResponseDTO.class);

    if (response == null) {
      return null;
    }

    return response.getCertificate();
  }

  private CreateCertificateRequestDTO convertCertificateType(CreateCertificateRequestDTO request) {
    if (request.getCertificateModelId() == null) {
      return request;
    }

    final var originalType = request.getCertificateModelId().getType();
    final var convertedType = getCertificateServiceTypeId(originalType);

    if (originalType.equals(convertedType)) {
      return request;
    }

    final var convertedModelId =
        CertificateModelIdDTO.builder()
            .type(convertedType)
            .version(request.getCertificateModelId().getVersion())
            .build();

    return CreateCertificateRequestDTO.builder()
        .user(request.getUser())
        .patient(request.getPatient())
        .careUnit(request.getCareUnit())
        .unit(request.getUnit())
        .careProvider(request.getCareProvider())
        .certificateModelId(convertedModelId)
        .fillType(request.getFillType())
        .status(request.getStatus())
        .externalReference(request.getExternalReference())
        .prefillXml(request.getPrefillXml())
        .build();
  }

  public List<CertificateType> getSupportedTypes() {
    final var url = baseUrl + TESTABILITY_CERTIFICATE_ENDPOINT_URL + SUPPORTED_TYPES;

    final var response =
        restClient
            .get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(CertificateType[].class);

    return Arrays.asList(Objects.requireNonNull(response));
  }

  public List<CertificateModelIdDTO> certificateTypeExists(String certificateType) {
    final var certificateServiceTypeId = getCertificateServiceTypeId(certificateType);
    final var url =
        baseUrl
            + TESTABILITY_CERTIFICATE_ENDPOINT_URL
            + SUPPORTED_TYPES
            + "/"
            + certificateServiceTypeId;

    final var response =
        restClient
            .get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(GetCertificateTypeVersionsResponse.class);

    if (response == null) {
      return List.of();
    }

    return response.getCertificateModelIds();
  }

  private String getCertificateServiceTypeId(String type) {
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
