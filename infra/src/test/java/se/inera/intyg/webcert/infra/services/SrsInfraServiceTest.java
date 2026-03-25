/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.infra.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.CollectionUtils;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.Samtyckesstatus;
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsRequestType;
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Utdatafilter;
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardsrekommendationstatus;
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Statistikstatus;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;
import se.inera.intyg.infra.integration.srs.model.SrsForDiagnosisResponse;
import se.inera.intyg.infra.integration.srs.model.SrsQuestion;
import se.inera.intyg.infra.integration.srs.model.SrsQuestionResponse;
import se.inera.intyg.infra.integration.srs.model.SrsResponse;
import se.inera.intyg.infra.integration.srs.stub.GetPredictionQuestionsStub;
import se.inera.intyg.infra.integration.srs.stub.repository.ConsentRepository;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum;

@SpringJUnitConfig(locations = "classpath:SrsServiceTest/test-context.xml")
class SrsInfraServiceTest {

  private static final String PNR_VALID = "191212121212";
  private static final String PNR_INVALID = "1912121212";

  @Autowired private SrsInfraService service;

  @Autowired private ConsentRepository consentRepository;

  private Utdatafilter utdatafilter;

  @BeforeEach
  void setup() {
    consentRepository.clear();
    utdatafilter = new Utdatafilter();
    utdatafilter.setPrediktion(false);
    utdatafilter.setAtgardsrekommendation(false);
    utdatafilter.setStatistik(false);
  }

  @Test
  void testNoneWithSRSDiagnosis() throws Exception {
    SrsResponse response =
        service.getSrs(
            createUser(),
            createPnr(PNR_VALID),
            Arrays.asList(new SrsCertificate("intygId", "M18", null)),
            utdatafilter,
            Arrays.asList(SrsQuestionResponse.create("questionId", "answerId")),
            null);
    assertNull(response.getStatistikNationellStatistik());
    assertNull(response.getAtgarderObs());
    assertNull(response.getAtgarderRek());
    assertNotNull(response.getPredictions());
    assertEquals(1, response.getPredictions().size());
    assertNull(response.getPredictions().get(0).getLevel());
    assertNull(response.getPredictions().get(0).getDescription());
    assertNotNull(response.getPredictions().get(0).getPrevalence());
  }

  @Test
  void testNoneWithUnknownDiagnosis() throws Exception {
    SrsResponse response =
        service.getSrs(
            createUser(),
            createPnr(PNR_VALID),
            Arrays.asList(new SrsCertificate("intygId", "X99", null)),
            utdatafilter,
            Arrays.asList(SrsQuestionResponse.create("questionId", "answerId")),
            15);
    assertNull(response.getStatistikNationellStatistik());
    assertNull(response.getAtgarderObs());
    assertNull(response.getAtgarderRek());
    assertNotNull(response.getPredictions());
    assertEquals(1, response.getPredictions().size());
    assertNull(response.getPredictions().get(0).getLevel());
    assertNull(response.getPredictions().get(0).getDescription());
    assertNull(response.getPredictions().get(0).getPrevalence());
  }

  @Test
  void testSrsPrediktion() throws Exception {
    utdatafilter.setPrediktion(true);
    SrsResponse response =
        service.getSrs(
            createUser(),
            createPnr(PNR_VALID),
            Arrays.asList(new SrsCertificate("intygId", "M18", null)),
            utdatafilter,
            Arrays.asList(SrsQuestionResponse.create("questionId", "answerId")),
            15);
    assertNotNull(response);
    assertNotNull(response.getPredictions());
    assertEquals(1, response.getPredictions().size());
    assertEquals(Integer.valueOf(1), response.getPredictions().get(0).getLevel());
    assertEquals("test", response.getPredictions().get(0).getDescription());
    assertNull(response.getAtgarderRek());
    assertNull(response.getAtgarderObs());
  }

  @Test
  void testSrsStatistik() throws Exception {
    utdatafilter.setStatistik(true);
    SrsResponse response =
        service.getSrs(
            createUser(),
            createPnr(PNR_VALID),
            Arrays.asList(new SrsCertificate("intygId", "M18", null)),
            utdatafilter,
            Arrays.asList(SrsQuestionResponse.create("questionId", "answerId")),
            15);
    assertNotNull(response.getStatistikNationellStatistik());
    assertNull(response.getAtgarderRek());
    assertNull(response.getAtgarderObs());
    assertNotNull(response.getPredictions());
    assertEquals(1, response.getPredictions().size());
    assertNull(response.getPredictions().get(0).getLevel());
    assertNull(response.getPredictions().get(0).getDescription());
  }

  @Test
  void testSrsPrediktionAndAtgardRekommendation() throws Exception {
    utdatafilter.setPrediktion(true);
    utdatafilter.setAtgardsrekommendation(true);
    SrsResponse response =
        service.getSrs(
            createUser(),
            createPnr(PNR_VALID),
            Arrays.asList(new SrsCertificate("intygId", "M18", null)),
            utdatafilter,
            Arrays.asList(SrsQuestionResponse.create("questionId", "answerId")),
            15);
    assertNotNull(response);
    assertNotNull(response.getPredictions());
    assertEquals(1, response.getPredictions().size());
    assertEquals(Integer.valueOf(1), response.getPredictions().get(0).getLevel());
    assertEquals("test", response.getPredictions().get(0).getDescription());
    assertNotNull(response.getAtgarderRek().get(0));
    assertNotNull(response.getAtgarderRek().get(1));
    assertNotNull(response.getAtgarderRek().get(2));
  }

  @Test
  void testSrsAll() throws Exception {
    utdatafilter.setAtgardsrekommendation(true);
    utdatafilter.setPrediktion(true);
    utdatafilter.setStatistik(true);
    SrsResponse response =
        service.getSrs(
            createUser(),
            createPnr(PNR_VALID),
            Arrays.asList(new SrsCertificate("intygId", "M18", null)),
            utdatafilter,
            Arrays.asList(SrsQuestionResponse.create("questionId", "answerId")),
            15);
    assertNotNull(response);
    assertNotNull(response.getPredictions());
    assertEquals(1, response.getPredictions().size());
    assertEquals(Integer.valueOf(1), response.getPredictions().get(0).getLevel());
    assertEquals("test", response.getPredictions().get(0).getDescription());

    assertEquals(3, response.getAtgarderRek().size());
    assertNotNull(response.getAtgarderRek().get(0));
    assertEquals("Atgardsforslag REK 1", response.getAtgarderRek().get(0).getRecommendationText());
    assertEquals("Atgardsforslag REK 2", response.getAtgarderRek().get(1).getRecommendationText());
    assertEquals("Atgardsforslag REK 3", response.getAtgarderRek().get(2).getRecommendationText());

    assertEquals(3, response.getAtgarderObs().size());
    assertNotNull(response.getAtgarderObs().get(0));
    assertEquals("Atgardsforslag OBS 1", response.getAtgarderObs().get(0).getRecommendationText());
    assertEquals("Atgardsforslag OBS 2", response.getAtgarderObs().get(1).getRecommendationText());
    assertEquals("Atgardsforslag OBS 3", response.getAtgarderObs().get(2).getRecommendationText());
    assertNotNull(response.getStatistikNationellStatistik());
  }

  @Test
  void testGetQuestions() {
    // Use reflection to spy on the stub to make sure we are using the correct request
    GetPredictionQuestionsResponderInterface spy = Mockito.spy(new GetPredictionQuestionsStub());
    ReflectionTestUtils.setField(service, "getPrediction", spy);

    final String diagnosisCode = "diagnosisCode";
    final String modelVersion = "2.2";
    List<SrsQuestion> response = service.getQuestions(diagnosisCode, modelVersion);
    assertFalse(CollectionUtils.isEmpty(response));

    ArgumentCaptor<GetPredictionQuestionsRequestType> captor =
        ArgumentCaptor.forClass(GetPredictionQuestionsRequestType.class);

    verify(spy).getPredictionQuestions(captor.capture());
    verifyNoMoreInteractions(spy);

    assertEquals(diagnosisCode, captor.getValue().getDiagnos().getCode());
    assertEquals("1.2.752.116.1.1.1.1.3", captor.getValue().getDiagnos().getCodeSystem());
  }

  @Test
  void testGetConsent() throws InvalidPersonNummerException {
    // Use reflection to spy on the stub to make sure we are using the correct request
    final String hsaId = "hsa";
    final Personnummer persNr = createPnr(PNR_INVALID);
    Samtyckesstatus response = service.getConsent(hsaId, persNr);
    assertNotNull(response);
    assertEquals(Samtyckesstatus.INGET, response);
  }

  @Test
  void testSetConsent() throws Exception {
    final String hsaId = "hsa";
    final Personnummer persNr = createPnr(PNR_VALID);
    ResultCodeEnum response = service.setConsent(hsaId, persNr, true);
    assertNotNull(response);
    assertEquals(ResultCodeEnum.OK, response);
  }

  @Test
  void testGetDiagnosisCodes() {
    List<String> response = service.getAllDiagnosisCodes(null);
    assertNotNull(response);
    assertEquals(3, response.size());
    assertTrue(response.contains("M18"));
    assertTrue(response.contains("J20"));
    assertTrue(response.contains("Q10"));
  }

  @Test
  void testGetSRSForDiagnosisCode() {
    final SrsForDiagnosisResponse response = service.getSrsForDiagnose("M18");
    assertNotNull(response);
    assertNotNull(response.getDiagnosisCode());
    assertEquals("M18", response.getDiagnosisCode());
    assertEquals(3, response.getAtgarderObs().size());
    assertEquals(3, response.getAtgarderRek().size());
    assertEquals(Atgardsrekommendationstatus.OK.name(), response.getAtgarderStatusCode());
    assertEquals(Statistikstatus.OK.name(), response.getStatistikStatusCode());
  }

  @Test
  void testGetSRSForHigherDiagnosisCode() {
    final SrsForDiagnosisResponse response = service.getSrsForDiagnose("M18.1");
    assertNotNull(response);
    assertNotNull(response.getDiagnosisCode());
    assertEquals("M18", response.getDiagnosisCode());
    assertEquals(3, response.getAtgarderObs().size());
    assertEquals(3, response.getAtgarderRek().size());
    assertEquals(
        Atgardsrekommendationstatus.DIAGNOSKOD_PA_HOGRE_NIVA.name(),
        response.getAtgarderStatusCode());

    assertEquals(
        Statistikstatus.DIAGNOSKOD_PA_HOGRE_NIVA.name(), response.getStatistikStatusCode());
    assertEquals("M18", response.getStatistikDiagnosisCode());
  }

  @Test
  void testGetSRSForUnknownDiagnosisCode() {
    final SrsForDiagnosisResponse response = service.getSrsForDiagnose("XX18");
    assertNotNull(response);
    assertNull(response.getDiagnosisCode());
    assertEquals(0, response.getAtgarderObs().size());
    assertEquals(0, response.getAtgarderRek().size());
    assertEquals(
        Atgardsrekommendationstatus.INFORMATION_SAKNAS.name(), response.getAtgarderStatusCode());
    assertEquals(Statistikstatus.STATISTIK_SAKNAS.name(), response.getStatistikStatusCode());
  }

  @Test
  void testGetSRSForDiagnosisCodeInvalidRequest() {
    assertThrows(IllegalArgumentException.class, () -> service.getSrsForDiagnose(null));
  }

  @Test
  void testGetPostNummerVardenhetVald() {
    SrsInfraServiceImpl svc = new SrsInfraServiceImpl();
    String postnummer = svc.getPostnummer(createUserVardenhetVald("111 11", null));
    assertEquals("11111", postnummer);
  }

  @Test
  void testGetPostNummerVardenhetValdNoPostnummerAtVardenhet() {
    SrsInfraServiceImpl svc = new SrsInfraServiceImpl();
    IntygUser user = createUserVardenhetVald(null, "222 22");
    assertThrows(IllegalArgumentException.class, () -> svc.getPostnummer(user));
  }

  @Test
  void testGetPostNummerVardenhetValdNoPostnummer() {
    SrsInfraServiceImpl svc = new SrsInfraServiceImpl();
    IntygUser user = createUserVardenhetVald(null, null);
    assertThrows(IllegalArgumentException.class, () -> svc.getPostnummer(user));
  }

  @Test
  void testGetPostNummerMottagningVald() {
    SrsInfraServiceImpl svc = new SrsInfraServiceImpl();
    String postnummer = svc.getPostnummer(createUserMottagningVald("111 11", "222 22"));
    assertEquals("22222", postnummer);
  }

  @Test
  void testGetPostNummerFromParentMottagningVald() {
    SrsInfraServiceImpl svc = new SrsInfraServiceImpl();
    String postnummer = svc.getPostnummer(createUserMottagningVald("111 11", null));
    assertEquals("11111", postnummer);
  }

  @Test
  void testGetPostNummerMottagningValdNoPostnummer() {
    SrsInfraServiceImpl svc = new SrsInfraServiceImpl();
    IntygUser user = createUserMottagningVald(null, null);
    assertThrows(IllegalArgumentException.class, () -> svc.getPostnummer(user));
  }

  @Test
  void testGetPostnummerVardenhetValdIncorrecLength() {
    SrsInfraServiceImpl svc = new SrsInfraServiceImpl();
    IntygUser user = createUserVardenhetVald("111 11111", null);
    assertThrows(IllegalArgumentException.class, () -> svc.getPostnummer(user));
  }

  @Test
  void testGetPostnummerFromParentMottagningValdIncorrectLength() {
    SrsInfraServiceImpl svc = new SrsInfraServiceImpl();
    IntygUser user = createUserMottagningVald("111 11111", null);
    assertThrows(IllegalArgumentException.class, () -> svc.getPostnummer(user));
  }

  @Test
  void testGetPostnummerMottagningValdIncorrectLength() {
    SrsInfraServiceImpl svc = new SrsInfraServiceImpl();
    IntygUser user = createUserMottagningVald("111 11", "222 22222");
    assertThrows(IllegalArgumentException.class, () -> svc.getPostnummer(user));
  }

  @Test
  void formatPostnummer() {
    SrsInfraServiceImpl svc = new SrsInfraServiceImpl();
    assertEquals("11111", svc.formatPostnummer("111 11"));
  }

  @Test
  void formatPostnummerIncorrectLength() {
    SrsInfraServiceImpl svc = new SrsInfraServiceImpl();
    assertThrows(IllegalArgumentException.class, () -> svc.formatPostnummer("1111"));
  }

  @Test
  void formatPostnummerNull() {
    SrsInfraServiceImpl svc = new SrsInfraServiceImpl();
    assertThrows(IllegalArgumentException.class, () -> svc.formatPostnummer(null));
  }

  @Test
  void getPostnummerMottagning() {
    Mottagning mt = createMottagning("mtId", "veId", "222 22");
    Vardenhet ve = createVardenhet("veId", Lists.newArrayList(mt), "111 11");
    Vardgivare vg = createVardgivare("vgId", Lists.newArrayList(ve));
    SrsInfraServiceImpl svc = new SrsInfraServiceImpl();
    assertEquals("222 22", svc.getPostnummer(mt, vg));
  }

  @Test
  void getPostnummerFromParent() {
    Mottagning mt = createMottagning("mtId", "veId", null);
    Vardenhet ve = createVardenhet("veId", Lists.newArrayList(mt), "111 11");
    Vardgivare vg = createVardgivare("vgId", Lists.newArrayList(ve));
    SrsInfraServiceImpl svc = new SrsInfraServiceImpl();
    assertEquals("111 11", svc.getPostnummer(mt, vg));
  }

  @Test
  void getPostnummerNoPostnummer() {
    Mottagning mt = createMottagning("mtId", "veId", null);
    Vardenhet ve = createVardenhet("veId", Lists.newArrayList(mt), null);
    Vardgivare vg = createVardgivare("vgId", Lists.newArrayList(ve));
    SrsInfraServiceImpl svc = new SrsInfraServiceImpl();
    assertNull(svc.getPostnummer(mt, vg));
  }

  private Personnummer createPnr(String pnr) {
    return Personnummer.createPersonnummer(pnr).get();
  }

  private IntygUser createUser() {
    return createUserMottagningVald("111 11", null);
  }

  private IntygUser createUserMottagningVald(
      String postnummerVardenhet, String postnummerMottagning) {
    IntygUser user = new IntygUser("hsaId");

    Mottagning mt = createMottagning("mtId", "veId", postnummerMottagning);
    Vardenhet ve = createVardenhet("veId", Lists.newArrayList(mt), postnummerVardenhet);
    Vardgivare vg = createVardgivare("vgId", Lists.newArrayList(ve));

    user.setValdVardenhet(mt);
    user.setValdVardgivare(vg);
    return user;
  }

  private IntygUser createUserVardenhetVald(
      String postnummerVardenhet, String postnummerMottagning) {
    IntygUser user = new IntygUser("hsaId");

    Mottagning mt = createMottagning("mtId", "veId", postnummerMottagning);
    Vardenhet ve = createVardenhet("veId", Lists.newArrayList(mt), postnummerVardenhet);
    Vardgivare vg = createVardgivare("vgId", Lists.newArrayList(ve));

    user.setValdVardenhet(ve);
    user.setValdVardgivare(vg);
    return user;
  }

  private Mottagning createMottagning(String id, String parentId, String postnummer) {
    Mottagning mt = new Mottagning();
    mt.setId(id);
    mt.setParentHsaId(parentId);
    mt.setPostnummer(postnummer);
    return mt;
  }

  private Vardenhet createVardenhet(String id, List<Mottagning> mottagningar, String postnummer) {
    Vardenhet ve = new Vardenhet();
    ve.setId(id);
    ve.setMottagningar(mottagningar);
    ve.setPostnummer(postnummer);
    return ve;
  }

  private Vardgivare createVardgivare(String id, List<Vardenhet> vardenheter) {
    Vardgivare vg = new Vardgivare();
    vg.setId(id);
    vg.setVardenheter(vardenheter);
    return vg;
  }
}
