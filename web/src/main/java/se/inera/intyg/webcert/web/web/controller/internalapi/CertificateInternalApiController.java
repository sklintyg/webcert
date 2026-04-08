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
package se.inera.intyg.webcert.web.web.controller.internalapi;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetCertificateIntegrationRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetCertificateResponse;

@RestController
@RequestMapping("/internalapi/certificate")
public class CertificateInternalApiController {

  private final GetCertificateInteralApi getCertificateInternalAggregator;
  private final GetCertificatePdfService getCertificateInternalPdfAggregator;

  public CertificateInternalApiController(
      @Qualifier("getCertificateInternalAggregator") GetCertificateInteralApi getCertificateInternalAggregator,
      @Qualifier("getCertificateInternalPdfAggregator") GetCertificatePdfService getCertificateInternalPdfAggregator) {
    this.getCertificateInternalAggregator = getCertificateInternalAggregator;
    this.getCertificateInternalPdfAggregator = getCertificateInternalPdfAggregator;
  }

  @PostMapping("/{certificateId}")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-internal-get-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public GetCertificateResponse getCertificate(
      @RequestBody GetCertificateIntegrationRequestDTO request,
      @PathVariable("certificateId") String certificateId) {
    return getCertificateInternalAggregator.get(certificateId, request.getPersonId());
  }

  @PostMapping("/{certificateId}/pdf")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-internal-get-pdf-data",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public CertificatePdfResponseDTO getPdfData(
      @RequestBody CertificatePdfRequestDTO request,
      @PathVariable("certificateId") String certificateId) {
    return getCertificateInternalPdfAggregator.get(
        request.getCustomizationId(), certificateId, request.getPersonId());
  }
}
