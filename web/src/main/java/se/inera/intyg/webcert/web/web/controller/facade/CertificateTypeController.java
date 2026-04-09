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
package se.inera.intyg.webcert.web.web.controller.facade;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.facade.GetCertificateTypeInfoModalFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateTypesFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoModalDTO;

@RestController
@RequestMapping("/api/certificate/type")
public class CertificateTypeController {

  private static final Logger LOG = LoggerFactory.getLogger(CertificateTypeController.class);

  private static final String UTF_8_CHARSET = ";charset=utf-8";

  private final GetCertificateTypesFacadeService getCertificateTypesFacadeService;
  private final GetCertificateTypeInfoModalFacadeService getCertificateTypeInfoModalFacadeService;

  public CertificateTypeController(
      @Qualifier("certificateTypeInfoAggregator") GetCertificateTypesFacadeService getCertificateTypesFacadeService,
      GetCertificateTypeInfoModalFacadeService getCertificateTypeInfoModalFacadeService) {
    this.getCertificateTypesFacadeService = getCertificateTypesFacadeService;
    this.getCertificateTypeInfoModalFacadeService = getCertificateTypeInfoModalFacadeService;
  }

  @GetMapping("/{patientId}")
  @PerformanceLogging(
      eventAction = "certificate-type-get-certificate-types",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<List<CertificateTypeInfoDTO>> getCertificateTypes(
      @PathVariable("patientId") @NotNull String patientId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Retrieving certificate types for patient");
    }
    try {
      final var certificateTypes =
          getCertificateTypesFacadeService.get(createPersonnummer(patientId));
      return ResponseEntity.ok(certificateTypes);
    } catch (InvalidPersonNummerException e) {
      LOG.error(e.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  @GetMapping("/modal/{certificateType}/{patientId}")
  @PerformanceLogging(
      eventAction = "certificate-type-get-info-modal",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<CertificateTypeInfoModalDTO> getCertificateTypeInfoModal(
      @PathVariable("certificateType") @NotNull String certificateType,
      @PathVariable("patientId") @NotNull String patientId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(
          "Retrieving certificate type info modal for certificate type: {} and patient",
          certificateType);
    }
    try {
      final var modal =
          getCertificateTypeInfoModalFacadeService.get(
              certificateType, createPersonnummer(patientId));
      if (modal == null) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
      }
      return ResponseEntity.ok(modal);
    } catch (InvalidPersonNummerException e) {
      LOG.error(e.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  private Personnummer createPersonnummer(String personId) throws InvalidPersonNummerException {
    return Personnummer.createPersonnummer(personId)
        .orElseThrow(
            () -> new InvalidPersonNummerException("Could not parse personnummer: " + personId));
  }
}
