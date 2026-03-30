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
package se.inera.intyg.webcert.web.web.controller.moduleapi;

import io.swagger.annotations.Api;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.csintegration.aggregate.PrintCertificateAggregator;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;

/**
 * Controller exposing services to be used by modules.
 *
 * @author nikpet
 */
@RestController
@RequestMapping("/moduleapi/intyg")
@Api(value = "/moduleapi/intyg", produces = "application/json")
public class IntygModuleApiController extends AbstractApiController {

  private static final Logger LOG = LoggerFactory.getLogger(IntygModuleApiController.class);

  private static final String CONTENT_DISPOSITION = "Content-Disposition";

  @Autowired private PrintCertificateAggregator printCertificateAggregator;

  /**
   * Return the signed certificate identified by the given id as PDF.
   *
   * @param intygsTyp the type of certificate
   * @param intygsId - the globally unique id of a certificate.
   * @return The certificate in PDF format
   */
  @GetMapping("/{intygsTyp}/{intygsId}/pdf")
  @PerformanceLogging(
      eventAction = "intyg-module-get-certificate-as-pdf",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public final ResponseEntity<byte[]> getIntygAsPdf(
      @PathVariable("intygsTyp") String intygsTyp,
      @PathVariable("intygsId") final String intygsId,
      HttpServletRequest request) {
    return getPdf(intygsTyp, intygsId, false, request);
  }

  /**
   * Return the signed certificate identified by the given id as PDF suited for the employer of the
   * patient.
   *
   * @param intygsTyp the type of certificate
   * @param intygsId - the globally unique id of a certificate.
   * @return The certificate in PDF format
   */
  @GetMapping("/{intygsTyp}/{intygsId}/pdf/arbetsgivarutskrift")
  @PerformanceLogging(
      eventAction = "intyg-module-get-certificate-as-pdf-for-employer",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public final ResponseEntity<byte[]> getIntygAsPdfForEmployer(
      @PathVariable("intygsTyp") String intygsTyp,
      @PathVariable("intygsId") final String intygsId,
      HttpServletRequest request) {
    return getPdf(intygsTyp, intygsId, true, request);
  }

  private ResponseEntity<byte[]> getPdf(
      String intygsTyp, final String intygsId, boolean isEmployerCopy, HttpServletRequest request) {
    if (!isEmployerCopy) {
      LOG.debug("Fetching signed intyg '{}' as PDF", intygsId);
    } else {
      LOG.debug("Fetching signed intyg '{}' as PDF for employer", intygsId);
    }

    final var response = printCertificateAggregator.get(intygsId, intygsTyp, isEmployerCopy);

    final var userAgent = request.getHeader("User-Agent");
    final var contentDisposition =
        userAgent.matches(".*Trident/\\d+.*|.*MSIE \\d+.*")
            ? buildPdfHeader(response.getFilename())
            : "inline";

    return ResponseEntity.ok()
        .header(CONTENT_DISPOSITION, contentDisposition)
        .body(response.getPdfData());
  }

  private String buildPdfHeader(String pdfFileName) {
    return "attachment; filename=\"" + pdfFileName + "\"";
  }
}
