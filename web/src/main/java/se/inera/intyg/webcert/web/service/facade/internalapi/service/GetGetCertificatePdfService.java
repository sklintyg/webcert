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

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.webcert.print.CertificatePrintService;
import se.inera.intyg.webcert.print.api.PrintException;
import se.inera.intyg.webcert.print.api.PrintRequest;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfResponseDTO;

@Service("getCertificateInternalPdfFromWC")
public class GetGetCertificatePdfService
    implements se.inera.intyg.webcert.web.web.controller.internalapi.GetCertificatePdfService {

  private final GetRequiredFieldsForCertificatePdfService getRequiredFieldsForCertificatePdfService;
  private final CertificatePrintService certificatePrintService;

  private static final String DONT_DISPLAY_DIAGNOSIS_ID = "!diagnoser";
  private static final List<String> OPTIONAL_FIELDS =
      List.of(DONT_DISPLAY_DIAGNOSIS_ID, "!onskarFormedlaDiagnos");

  public GetGetCertificatePdfService(
      GetRequiredFieldsForCertificatePdfService getRequiredFieldsForCertificatePdfService,
      CertificatePrintService certificatePrintService) {
    this.getRequiredFieldsForCertificatePdfService = getRequiredFieldsForCertificatePdfService;
    this.certificatePrintService = certificatePrintService;
  }

  @Override
  public CertificatePdfResponseDTO get(
      String customizationId, String certificateId, String personId) {
    final var requiredFieldsForCertificatePdf =
        getRequiredFieldsForCertificatePdfService.get(certificateId);

    final var employerCopy = DONT_DISPLAY_DIAGNOSIS_ID.equals(customizationId);
    final var request =
        new PrintRequest(
            requiredFieldsForCertificatePdf.getCertificateType(),
            requiredFieldsForCertificatePdf.getCertificateTypeVersion(),
            requiredFieldsForCertificatePdf.getInternalJsonModel(),
            requiredFieldsForCertificatePdf.getStatuses(),
            ApplicationOrigin.MINA_INTYG,
            requiredFieldsForCertificatePdf.getStatus(),
            employerCopy,
            employerCopy ? OPTIONAL_FIELDS : Collections.emptyList());

    try {
      final var result = certificatePrintService.print(request);
      return CertificatePdfResponseDTO.create(result.filename(), result.pdfData());
    } catch (PrintException exception) {
      throw new IllegalStateException(
          "Unable to get pdf from module api implementation", exception);
    }
  }
}
