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
package se.inera.intyg.webcert.web.service.facade.internalapi.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.PdfResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.web.service.certificate.GetCertificateService;
import se.inera.intyg.webcert.web.service.facade.impl.GetCertificateFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.BinaryCertificateMetadataConverter;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.GetBinaryCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.GetBinaryCertificate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetBinaryCertificateFromWC implements GetBinaryCertificate {

  private final GetRequiredFieldsForCertificatePdfService getRequiredFieldsForCertificatePdfService;
  private final IntygModuleRegistry moduleRegistry;
  private final GetCertificateFacadeServiceImpl getCertificateFacadeService;
  private final GetCertificateService getCertificateService;
  private final BinaryCertificateMetadataConverter binaryCertificateMetadataConverter;

  @Override
  public GetBinaryCertificateResponseDTO get(String certificateId) {
    final var pdfData = getPdfData(certificateId);
    final var certificate = getCertificateFacadeService.getCertificate(certificateId, false, false);
    if (certificate.getMetadata().getStatus().equals(CertificateStatus.UNSIGNED)) {
      throw new IllegalStateException(
          "Certificate is draft and not eligible to export as BinaryCertificate");
    }

    final var certificateAsIntyg =
        getCertificateService.getCertificateAsIntyg(
            certificateId, certificate.getMetadata().getType());

    return GetBinaryCertificateResponseDTO.builder()
        .pdfData(pdfData)
        .metadata(
            binaryCertificateMetadataConverter.toBinaryCertificate(certificateAsIntyg, certificate))
        .build();
  }

  private byte[] getPdfData(String certificateId) {
    final var requiredFieldsForCertificatePdf =
        getRequiredFieldsForCertificatePdfService.get(certificateId);

    final var moduleApi =
        getModuleApi(
            requiredFieldsForCertificatePdf.getCertificateType(),
            requiredFieldsForCertificatePdf.getCertificateTypeVersion());

    final var pdfResponse =
        getPdfResponse(
            moduleApi,
            requiredFieldsForCertificatePdf.getInternalJsonModel(),
            requiredFieldsForCertificatePdf.getStatuses(),
            requiredFieldsForCertificatePdf.getStatus());

    return pdfResponse.getPdfData();
  }

  private PdfResponse getPdfResponse(
      ModuleApi moduleApi, String jsonModel, List<Status> statuses, UtkastStatus status) {
    try {
      return moduleApi.pdf(jsonModel, statuses, ApplicationOrigin.WEBCERT, status);
    } catch (ModuleException exception) {
      throw new IllegalStateException(
          "Unable to get pdf from module api implementation", exception);
    }
  }

  private ModuleApi getModuleApi(String certificateType, String certificateTypeVersion) {
    try {
      return moduleRegistry.getModuleApi(certificateType, certificateTypeVersion);
    } catch (ModuleNotFoundException exception) {
      throw new IllegalStateException(
          String.format(
              "Module api not found with typeVersion '%s' and type '%s'",
              certificateTypeVersion, certificateType),
          exception);
    }
  }
}
