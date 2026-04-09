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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.facade.ValidateSickLeavePeriodFacadeService;
import se.inera.intyg.webcert.web.service.fmb.FmbDiagnosInformationService;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbResponse;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateSickLeavePeriodRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateSickLeavePeriodResponseDTO;

@RestController
@RequestMapping("/api/fmb")
@RequiredArgsConstructor
public class FMBController {

  private static final Logger LOG = LoggerFactory.getLogger(FMBController.class);
  private final ValidateSickLeavePeriodFacadeService validateSickLeavePeriodFacadeService;
  private final FmbDiagnosInformationService fmbDiagnosInformationService;

  @GetMapping(value = "/{icd10}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PerformanceLogging(
      eventAction = "fmb-get-fmb-data-for-icd10",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<FmbResponse> getFmbForIcd10(@PathVariable("icd10") String icd10) {
    if (icd10 == null || icd10.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    return fmbDiagnosInformationService
        .findFmbDiagnosInformationByIcd10Kod(icd10)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @PostMapping("/validateSickLeavePeriod")
  @PerformanceLogging(
      eventAction = "fmb-validate-sickleave-period",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<ValidateSickLeavePeriodResponseDTO> validateSickLeavePeriod(
      @RequestBody @NotNull ValidateSickLeavePeriodRequestDTO validateSickLeavePeriod) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Validating sick leave period");
    }
    final var response =
        validateSickLeavePeriodFacadeService.validateSickLeavePeriod(validateSickLeavePeriod);
    return ResponseEntity.ok(ValidateSickLeavePeriodResponseDTO.create(response));
  }
}
