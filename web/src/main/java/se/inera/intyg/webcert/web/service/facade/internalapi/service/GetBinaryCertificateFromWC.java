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

import java.rmi.ServerException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.PdfResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.BinaryCertificateMetadataConverter;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.GetBinaryCertificateResponseDTO;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.internalapi.GetBinaryCertificate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetBinaryCertificateFromWC implements GetBinaryCertificate {

  private final IntygModuleRegistry moduleRegistry;
  private final BinaryCertificateMetadataConverter binaryCertificateMetadataConvertera;
  private final IntygService intygService;

  @Override
  public GetBinaryCertificateResponseDTO get(String certificateId) {

    try {
      final var content = intygService.fetchIntygDataForInternalUse(certificateId, true);

      if (content == null || content.getStatuses().isEmpty()) {
        log.warn("Requested certificate with id {} is a draft", certificateId);
        throw new ServerException("Server Error");
      }

      final var type = content.getUtlatande().getTyp();
      final var entrypoint = moduleRegistry.getModuleEntryPoint(type);
      final var pdfData = getPdfData(content);
      final var parentContent = getParentCertificate(content);
      final var metadata = binaryCertificateMetadataConvertera.toBinaryCertificate(
          content,
          entrypoint,
          parentContent);

      return GetBinaryCertificateResponseDTO.builder()
          .pdfData(pdfData)
          .metadata(metadata)
          .build();

    } catch(Exception e) {
      log.error(e.getMessage());
      return null;
    }
  }

  private IntygContentHolder getParentCertificate(IntygContentHolder contentholder) {
    return getParentCertificateId(contentholder.getRelations().getParent())
        .map(id -> intygService.fetchIntygDataForInternalUse(id, false))
        .orElse(null);
  }

  private Optional<String> getParentCertificateId(WebcertCertificateRelation relation) {
    return Optional.ofNullable(relation)
        .map(WebcertCertificateRelation::getIntygsId);
  }

  private byte[] getPdfData(IntygContentHolder content) {
    final var moduleApi =
        getModuleApi(
            content.getUtlatande().getTyp(),
            content.getUtlatande().getTextVersion());

    final var pdfResponse =
        getPdfResponse(
            moduleApi,
            content.getContents(),
            content.getStatuses(),
            UtkastStatus.SIGNED);

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
