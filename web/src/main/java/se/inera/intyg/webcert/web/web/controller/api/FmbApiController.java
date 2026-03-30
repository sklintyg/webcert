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
package se.inera.intyg.webcert.web.web.controller.api;

import static java.util.Objects.isNull;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Optional;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.fmb.FmbDiagnosInformationService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.Icd10KoderRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.MaximalSjukskrivningstidRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.Period;

@RestController
@RequestMapping("/api/fmb")
@Api(
    value = "fmb",
    description = "REST API för Försäkringsmedicinskt beslutsstöd",
    produces = "application/json")
public class FmbApiController extends AbstractApiController {

  private static final int OK = 200;
  private static final int BAD_REQUEST = 400;

  @Autowired private FmbDiagnosInformationService fmbDiagnosInformationService;

  // CHECKSTYLE:OFF LineLength
  @GetMapping("/{icd10}")
  @ApiOperation(
      value = "Get FMB data for ICD10 codes",
      httpMethod = "GET",
      notes = "Fetch the admin user details",
      produces = "application/json")
  @ApiResponses(
      value = {
        @ApiResponse(
            code = OK,
            message = "Given FMB data for icd10 code found",
            response = FmbResponse.class),
        @ApiResponse(code = BAD_REQUEST, message = "Bad request due to missing icd10 code")
      })
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "fmb-get-fmb-data-for-icd10",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<Object> getFmbForIcd10(
      @ApiParam(value = "ICD10 code", required = true) @PathVariable("icd10") String icd10) {
    if (icd10 == null || icd10.isEmpty()) {
      return ResponseEntity.badRequest().body("Missing icd10 code");
    }

    return fmbDiagnosInformationService
        .findFmbDiagnosInformationByIcd10Kod(icd10)
        .<ResponseEntity<Object>>map(ResponseEntity::ok)
        .orElseGet(
            () -> ResponseEntity.status(org.springframework.http.HttpStatus.NO_CONTENT).body(null));
  }

  @GetMapping("/valideraSjukskrivningstid")
  @ApiOperation(
      value = "validate sjukskrivningstid for patient and ICD10 codes",
      httpMethod = "GET",
      produces = "application/json")
  @ApiResponses(
      value = {
        @ApiResponse(
            code = HttpStatus.SC_OK,
            message = "Response Object containing info regardning sjukskrivning for patient",
            response = FmbResponse.class),
        @ApiResponse(
            code = HttpStatus.SC_BAD_REQUEST,
            message =
                "Bad request due to missing icd10 codes, or missing foreslagenSjukskrivningstid")
      })
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "fmb-validate-sickleave-period",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<Object> valideraSjukskrivningstid(
      @ApiParam(value = "ICD10 code", required = true)
          @RequestParam(value = "icd10Kod1", required = false)
          final String icd10Kod1,
      @RequestParam(value = "icd10Kod2", required = false) final String icd10Kod2,
      @RequestParam(value = "icd10Kod3", required = false) final String icd10Kod3,
      @ApiParam(value = "Personnummer för patient", required = true)
          @RequestParam(value = "personnummer", required = false)
          final String personnummer,
      @ApiParam(value = "Sjukskrivningsperioder för föreslagen sjukskrivning", required = true)
          @RequestParam(value = "periods", required = false)
          final List<Period> periods) {

    List<String> validationErrors = Lists.newArrayList();

    if (isNull(icd10Kod1)) {
      validationErrors.add("Missing icd10 codes");
    }

    if (isNull(personnummer)) {
      validationErrors.add("Missing personnummer");
    }

    final Optional<Personnummer> optionalPersonnummer =
        Personnummer.createPersonnummer(personnummer);
    if (optionalPersonnummer.isEmpty()) {
      validationErrors.add("Incorrect personnummer format");
    }

    if (isNull(periods) || periods.isEmpty()) {
      validationErrors.add("Missing periods");
    }

    if (!validationErrors.isEmpty()) {
      return ResponseEntity.badRequest().body(String.join(",", validationErrors));
    }

    return ResponseEntity.ok(
        fmbDiagnosInformationService.validateSjukskrivningtidForPatient(
            MaximalSjukskrivningstidRequest.of(
                Icd10KoderRequest.of(icd10Kod1, icd10Kod2, icd10Kod3),
                optionalPersonnummer.get(),
                periods)));
  }
  // CHECKSTYLE:ON LineLength
}
