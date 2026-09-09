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
package se.inera.intyg.webcert.web.csintegration.certificate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateInternalPdfResponseDTO;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.GetBinaryCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.GetBinaryCertificate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetBinaryCertificateFromCS implements GetBinaryCertificate {

  private final CSIntegrationService csIntegrationService;

  @Override
  public GetBinaryCertificateResponseDTO get(String certificateId) {
    final var exists = csIntegrationService.certificateExists(certificateId);
    if (Boolean.FALSE.equals(exists)) {
      log.debug("Certificate with id '{}' does not exist in certificate service", certificateId);
      return null;
    }

    GetCertificateInternalPdfResponseDTO binaryCertificate = null;
    try {
      binaryCertificate = csIntegrationService.getBinaryCertificate(certificateId);
    } catch (CSClientException e) {
      log.error(
          "Failed to get binary certificate with id '{}' from certificate service",
          certificateId,
          e);
      if (e.isClientError()) {
        if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
          log.warn("Requested certificate with id {} is a draft", certificateId);
          throw new WebCertServiceException(
              WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
              "Requested certificate with id %s is a draft".formatted(certificateId));
        } else {
          throw new WebCertServiceException(
              WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
              "Failed to get binary certificate with id '"
                  + certificateId
                  + "' from certificate service due to client error");
        }
      } else if (e.isServerError()) {
        throw new WebCertServiceException(
            WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM,
            "Failed to get binary certificate with id '"
                + certificateId
                + "' from certificate service due to server error");
      } else {
        throw new WebCertServiceException(
            WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM,
            "Failed to get binary certificate with id '"
                + certificateId
                + "' from certificate service due to unknown problem");
      }
    }

    return GetBinaryCertificateResponseDTO.builder()
        .pdfData(binaryCertificate.getPdfData())
        .metadata(binaryCertificate.getMetadata())
        .build();
  }
}
