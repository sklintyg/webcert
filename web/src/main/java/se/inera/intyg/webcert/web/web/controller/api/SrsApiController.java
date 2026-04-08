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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.Samtyckesstatus;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.webcert.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.infra.srs.model.SrsForDiagnosisResponse;
import se.inera.intyg.webcert.infra.srs.model.SrsQuestion;
import se.inera.intyg.webcert.infra.srs.model.SrsQuestionResponse;
import se.inera.intyg.webcert.infra.srs.model.SrsResponse;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.srs.SrsService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum;

// CHECKSTYLE:OFF ParameterNumber
@RestController
@RequestMapping("/api/srs")
@Api(
    value = "srs",
    description = "REST API för Stöd för rätt sjukskrivning",
    produces = "application/json")
public class SrsApiController extends AbstractApiController {

  private static final Logger LOG = LoggerFactory.getLogger(SrsApiController.class);

  private static final int OK = 200;
  private static final int NO_CONTENT = 204;
  private static final int BAD_REQUEST = 400;

  @Autowired private SrsService srsService;

  @PostMapping("/{intygId}/{personnummer}/{diagnosisCode}")
  @ApiOperation(value = "Get SRS data", httpMethod = "POST", produces = "application/json")
  @ApiResponses(
      value = {
        @ApiResponse(code = OK, message = "SRS data found", response = SrsResponse.class),
        @ApiResponse(code = BAD_REQUEST, message = "Bad request"),
        @ApiResponse(code = NO_CONTENT, message = "No prediction model found")
      })
  @PrometheusTimeMethod
  @PerformanceLogging(eventAction = "srs-get-srs", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<SrsResponse> getSrs(
      @ApiParam(value = "Intyg id", required = true) @PathVariable("intygId") String intygId,
      @ApiParam(value = "Personnummer", required = true) @PathVariable("personnummer")
          String personnummer,
      @ApiParam(value = "Diagnosis Code", required = true) @PathVariable("diagnosisCode")
          String diagnosisCode,
      @ApiParam(value = "Utdatafilter: Prediktion")
          @RequestParam(value = "prediktion", required = false, defaultValue = "false")
          boolean prediktion,
      @ApiParam(value = "Utdatafilter: AtgardRekommendation")
          @RequestParam(value = "atgard", required = false, defaultValue = "false")
          boolean atgard,
      @ApiParam(value = "Utdatafilter: Statistik")
          @RequestParam(value = "statistik", required = false, defaultValue = "false")
          boolean statistik,
      @ApiParam(value = "Dag i sjukskrivning")
          @RequestParam(value = "daysIntoSickLeave", required = false, defaultValue = "15")
          Integer daysIntoSickLeave,
      @ApiParam(value = "Svar på frågor") @RequestBody(required = false)
          List<SrsQuestionResponse> questions) {
    authoritiesValidator
        .given(getWebCertUserService().getUser())
        .features(AuthoritiesConstants.FEATURE_SRS)
        .orThrow();
    LOG.debug(
        "getSrs(intygId: {}, diagnosisCode: {}, prediktion: {}, atgard: {}, statistik: {}, daysIntoSickLeave, {})",
        intygId,
        diagnosisCode,
        prediktion,
        atgard,
        statistik,
        daysIntoSickLeave);
    try {
      SrsResponse srsResponse =
          srsService.getSrs(
              getWebCertUserService().getUser(),
              intygId,
              personnummer,
              diagnosisCode,
              prediktion,
              atgard,
              statistik,
              questions,
              daysIntoSickLeave);
      return ResponseEntity.ok(srsResponse);
    } catch (InvalidPersonNummerException | IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(null);
    }
  }

  @GetMapping("/questions/{diagnosisCode}")
  @ApiOperation(
      value = "Get questions for diagnosis code",
      httpMethod = "GET",
      produces = "application/json")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "srs-get-question",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<List<SrsQuestion>> getQuestions(
      @ApiParam(value = "Diagnosis code") @PathVariable("diagnosisCode") String diagnosisCode,
      @ApiParam(value = "Prediction model version")
          @RequestParam(value = "modelVersion", required = false)
          String modelVersion) {
    authoritiesValidator
        .given(getWebCertUserService().getUser())
        .features(AuthoritiesConstants.FEATURE_SRS)
        .orThrow();
    try {
      List<SrsQuestion> questionList = srsService.getQuestions(diagnosisCode, modelVersion);
      return ResponseEntity.ok(questionList);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(null);
    }
  }

  @GetMapping("/consent/{personnummer}/{vardenhetHsaId}")
  @ApiOperation(
      value = "Get consent for patient and careunit",
      httpMethod = "GET",
      produces = "application/json")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "srs-get-consent",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<Samtyckesstatus> getConsent(
      @ApiParam(value = "Personnummer") @PathVariable("personnummer") String personnummer,
      @ApiParam(value = "HsaId för vårdenhet") @PathVariable("vardenhetHsaId")
          String careUnitHsaId) {
    authoritiesValidator
        .given(getWebCertUserService().getUser())
        .features(AuthoritiesConstants.FEATURE_SRS)
        .orThrow();
    try {
      Samtyckesstatus response = srsService.getConsent(careUnitHsaId, personnummer);
      return ResponseEntity.ok(response);
    } catch (InvalidPersonNummerException e) {
      return ResponseEntity.badRequest().body(null);
    }
  }

  @PutMapping("/consent/{personnummer}/{vardenhetHsaId}")
  @ApiOperation(
      value = "Set consent for patient and careunit",
      httpMethod = "PUT",
      produces = "application/json")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "srs-set-consent",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<ResultCodeEnum> setConsent(
      @ApiParam(value = "Personnummer") @PathVariable("personnummer") String personnummer,
      @ApiParam(value = "HsaId för vårdenhet") @PathVariable("vardenhetHsaId") String careUnitHsaId,
      @RequestBody boolean consent) {
    authoritiesValidator
        .given(getWebCertUserService().getUser())
        .features(AuthoritiesConstants.FEATURE_SRS)
        .orThrow();

    try {
      ResultCodeEnum result = srsService.setConsent(personnummer, careUnitHsaId, consent);
      return ResponseEntity.ok(result);
    } catch (InvalidPersonNummerException e) {
      return ResponseEntity.badRequest().body(null);
    }
  }

  @PutMapping("/opinion/{personnummer}/{vardgivareHsaId}/{vardenhetHsaId}/{intygId}/{diagnoskod}")
  @ApiOperation(
      value = "Set own opinion for risk prediction",
      httpMethod = "PUT",
      produces = "application/json")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "srs-set-own-opinion",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<ResultCodeEnum> setOwnOpinion(
      @ApiParam(value = "Personnummer") @PathVariable("personnummer") String personnummer,
      @ApiParam(value = "HSA-Id för vårdgivare") @PathVariable("vardgivareHsaId")
          String vardgivareHsaId,
      @ApiParam(value = "HSA-Id för vårdenhet") @PathVariable("vardenhetHsaId")
          String vardenhetHsaId,
      @ApiParam(value = "Intyg id", required = true) @PathVariable("intygId") String intygId,
      @ApiParam(value = "Diagnoskod", required = true) @PathVariable("diagnoskod")
          String diagnosisCode,
      @RequestBody String opinion) {
    authoritiesValidator
        .given(getWebCertUserService().getUser())
        .features(AuthoritiesConstants.FEATURE_SRS)
        .orThrow();

    try {
      ResultCodeEnum result =
          srsService.setOwnOpinion(
              personnummer, vardgivareHsaId, vardenhetHsaId, intygId, diagnosisCode, opinion);
      return ResponseEntity.ok(result);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(null);
    }
  }

  @GetMapping("/codes")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "srs-get-diagnosis-codes",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<List<String>> getDiagnosisCodes(
      @ApiParam(value = "Prediction model version")
          @RequestParam(value = "modelVersion", required = false)
          String modelVersion) {
    authoritiesValidator
        .given(getWebCertUserService().getUser())
        .features(AuthoritiesConstants.FEATURE_SRS)
        .orThrow();
    return ResponseEntity.ok(srsService.getAllDiagnosisCodes(modelVersion));
  }

  @GetMapping("/atgarder/{diagnosisCode}")
  @ApiOperation(
      value = "Get SRS info for diagnosecode",
      httpMethod = "GET",
      produces = "application/json")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "srs-get-srs-for-diagnosis-codes",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<SrsForDiagnosisResponse> getSrsForDiagnosisCodes(
      @PathVariable("diagnosisCode") String diagnosisCode) {
    authoritiesValidator
        .given(getWebCertUserService().getUser())
        .features(AuthoritiesConstants.FEATURE_SRS)
        .orThrow();
    SrsForDiagnosisResponse srsForDiagnose = srsService.getSrsForDiagnosis(diagnosisCode);
    return ResponseEntity.ok(srsForDiagnose);
  }
}
// CHECKSTYLE:ON ParameterNumber
