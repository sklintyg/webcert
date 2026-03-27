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
package se.inera.intyg.webcert.web.service.srs;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Utdatafilter;
import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.infra.srs.model.SrsCertificate;
import se.inera.intyg.webcert.infra.srs.model.SrsPrediction;
import se.inera.intyg.webcert.infra.srs.model.SrsQuestionResponse;
import se.inera.intyg.webcert.infra.srs.model.SrsRecommendation;
import se.inera.intyg.webcert.infra.srs.model.SrsResponse;
import se.inera.intyg.webcert.infra.srs.services.SrsInfraService;
import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class SrsServiceImplTest {

  private static Diagnos buildDiagnosis(String code, String description) {
    Diagnos diagnosis = new Diagnos();
    diagnosis.setKod(code);
    diagnosis.setBeskrivning(description);
    return diagnosis;
  }

  private static final Diagnos DIAGNOSIS_F438A = buildDiagnosis("F438A", "Utmattningssyndrom");
  private static final Diagnos DIAGNOSIS_F438 =
      buildDiagnosis("F438", "Andra specificerade reaktioner på svår stress");
  private static final Diagnos DIAGNOSIS_F43 =
      buildDiagnosis("F43", "Anpassningsstörningar och reaktion på svår stress");

  private static Utdatafilter buildUtdataFilter(
      boolean prediction, boolean measure, boolean statistics) {
    Utdatafilter f = new Utdatafilter();
    f.setAtgardsrekommendation(measure);
    f.setPrediktion(prediction);
    f.setStatistik(statistics);
    return f;
  }

  @Mock private WebCertUser user;

  @Mock private SrsInfraService srsInfraService;

  @Mock private LogService logService;

  @Mock private DiagnosService diagnosService;

  @Mock private IntygService intygService;

  @Mock private IntygModuleFacade intygModuleFacade;

  @Mock private SrsCertificateExtensionChainService srsCertificateExtensionChainService;

  @InjectMocks private SrsServiceImpl srsServiceUnderTest;

  @BeforeEach
  void init() throws Exception {
    SrsResponse srsResponse =
        new SrsResponse(
            asList(SrsRecommendation.create("please observe", "text")),
            asList(SrsRecommendation.create("recommended measure", "text")),
            asList(SrsRecommendation.create("extension measure", "text")),
            asList(SrsRecommendation.create("rehab measure", "text")),
            asList(
                new SrsPrediction(
                    "intyg-id-123",
                    "F438",
                    null,
                    "OK",
                    1,
                    "desc",
                    0.68,
                    0.54,
                    asList(SrsQuestionResponse.create("question1", "answer1")),
                    "KORREKT",
                    LocalDateTime.now(),
                    15,
                    "2.2")),
            "F438A",
            "OK",
            "F43",
            "OK",
            asList(13432, 37494, 50517, 62952, 71240));
    // Initialize descriptions to null when returned from infra mock, they are decorated in
    // SrsService.getSrs
    srsResponse.setStatistikDiagnosisDescription(null);
    srsResponse.setAtgarderDiagnosisDescription(null);

    SrsResponse srsResponseWithoutPrediction =
        new SrsResponse(
            asList(SrsRecommendation.create("please observe", "text")),
            asList(SrsRecommendation.create("recommended measure", "text")),
            asList(SrsRecommendation.create("extension measure", "text")),
            asList(SrsRecommendation.create("rehab measure", "text")),
            asList(
                new SrsPrediction(
                    "certId",
                    "F438",
                    null,
                    "OK",
                    1,
                    "desc",
                    null,
                    0.54,
                    null,
                    null,
                    LocalDateTime.now(),
                    null,
                    "2.2")),
            "F438A",
            "OK",
            "F43",
            "OK",
            asList(13432, 37494, 50517, 62952, 71240));

    // Initilize mock responses
    // full SRS response with prediction
    when(srsInfraService.getSrs(
            any(WebCertUser.class),
            any(Personnummer.class),
            anyList(),
            refEq(buildUtdataFilter(true, true, true)),
            anyList(),
            anyInt()))
        .thenReturn(srsResponse);
    // SRS response without prediction
    when(srsInfraService.getSrs(
            any(WebCertUser.class),
            any(Personnummer.class),
            anyList(),
            refEq(buildUtdataFilter(false, true, true)),
            anyList(),
            anyInt()))
        .thenReturn(srsResponseWithoutPrediction);

    when(diagnosService.getDiagnosisByCode("F438A", Diagnoskodverk.ICD_10_SE))
        .thenReturn(DiagnosResponse.ok(List.of(DIAGNOSIS_F438A), false));

    when(diagnosService.getDiagnosisByCode("F438", Diagnoskodverk.ICD_10_SE))
        .thenReturn(DiagnosResponse.ok(List.of(DIAGNOSIS_F438), false));

    when(diagnosService.getDiagnosisByCode("F43", Diagnoskodverk.ICD_10_SE))
        .thenReturn(DiagnosResponse.ok(List.of(DIAGNOSIS_F43), false));

    when(srsCertificateExtensionChainService.get("intyg-id-123"))
        .thenReturn(
            List.of(
                new SrsCertificate("intyg-id-123", "F438A", null, "parent-intyg-id-1"),
                new SrsCertificate("parent-intyg-id-1", "F438A", null, "parent-intyg-id-2"),
                new SrsCertificate("parent-intyg-id-2", "F438A", null, null)));

    when(srsCertificateExtensionChainService.get("parent-intyg-id-3"))
        .thenReturn(List.of(new SrsCertificate("parent-intyg-id-3", "F438A", null, null)));
  }

  @Test
  void getSrsMissingPersonalIdentityNumberShouldThrowException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          srsServiceUnderTest.getSrs(
              user,
              "intyg-id-123",
              "",
              "F438A",
              true,
              true,
              true,
              new ArrayList<SrsQuestionResponse>(),
              null);
        });
  }

  @Test
  void getSrsIllFormedPersonalIdentityNumberShouldThrowException() {
    assertThrows(
        InvalidPersonNummerException.class,
        () -> {
          srsServiceUnderTest.getSrs(
              user,
              "intyg-id-123",
              "incorrectform1912-12-12-1212",
              "F438A",
              true,
              true,
              true,
              new ArrayList<SrsQuestionResponse>(),
              15);
        });
  }

  @Test
  void getSrsMissingDiagnosisCodeShouldThrowException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          srsServiceUnderTest.getSrs(
              user,
              "intyg-id-123",
              "191212121212",
              "",
              true,
              true,
              true,
              new ArrayList<SrsQuestionResponse>(),
              15);
        });
  }

  @Test
  void getSrsShouldLogShowPredictionIfPredictionIsIncluded() throws Exception {
    srsServiceUnderTest.getSrs(
        user,
        "intyg-id-123",
        "191212121212",
        "F438A",
        true,
        true,
        true,
        new ArrayList<SrsQuestionResponse>(),
        15);
    verify(logService, times(1)).logShowPrediction("191212121212", "intyg-id-123");
  }

  @Test
  void getSrsShouldNotLogShowPredictionIfPredictionIsNotIncluded() throws Exception {
    srsServiceUnderTest.getSrs(
        user,
        "intyg-id-123",
        "191212121212",
        "F438A",
        false,
        true,
        true,
        new ArrayList<SrsQuestionResponse>(),
        15);
    verify(logService, times(0)).logShowPrediction("191212121212", "intyg-id-123");
  }

  @Test
  void getSrsShouldAddDiagnosisDescriptions() throws Exception {
    SrsResponse resp =
        srsServiceUnderTest.getSrs(
            user,
            "intyg-id-123",
            "191212121212",
            "F438A",
            true,
            true,
            true,
            new ArrayList<SrsQuestionResponse>(),
            15);
    assertNotNull(resp);
    assertEquals(
        resp.getPredictions().get(0).getDiagnosisDescription(),
        "Andra specificerade reaktioner på svår stress");
    assertEquals(resp.getAtgarderDiagnosisDescription(), "Utmattningssyndrom");
    assertEquals(
        resp.getStatistikDiagnosisDescription(),
        "Anpassningsstörningar och reaktion på svår stress");
  }

  @Test
  void getSrsShouldAddCertificateExtensionChainWithMaxThreeEntries() throws Exception {
    SrsResponse resp =
        srsServiceUnderTest.getSrs(
            user,
            "intyg-id-123",
            "191212121212",
            "F438A",
            true,
            true,
            true,
            new ArrayList<SrsQuestionResponse>(),
            15);
    assertNotNull(resp);
    assertNotNull(resp.getExtensionChain());
    assertEquals(3, resp.getExtensionChain().size());
    assertEquals(resp.getExtensionChain().get(0).getCertificateId(), "intyg-id-123");
    assertEquals(resp.getExtensionChain().get(1).getCertificateId(), "parent-intyg-id-1");
    assertEquals(resp.getExtensionChain().get(2).getCertificateId(), "parent-intyg-id-2");
  }

  @Test
  void getSrsShouldAddCertificateExtensionChainEvenIfNoExtension() throws Exception {
    SrsResponse resp =
        srsServiceUnderTest.getSrs(
            user,
            "parent-intyg-id-3",
            "191212121212",
            "F438A",
            false,
            true,
            true,
            new ArrayList<SrsQuestionResponse>(),
            null);
    assertNotNull(resp);
    assertNotNull(resp.getExtensionChain());
    assertEquals(1, resp.getExtensionChain().size());
    assertEquals(resp.getExtensionChain().get(0).getCertificateId(), "parent-intyg-id-3");
  }
}
