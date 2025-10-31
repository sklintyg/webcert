package se.inera.intyg.webcert.web.service.srs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.fkparent.model.internal.Diagnos;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.lisjp.v1.model.internal.LisjpUtlatandeV1;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.Relation;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;

@ExtendWith(MockitoExtension.class)
class GetSrsCertificateFromWebcertTest {

  @Mock
  private IntygModuleFacade intygModuleFacade;

  @Mock
  private IntygService intygService;

  private GetSrsCertificateFromWebcert getSrsCertificateFromWebcert;

  @BeforeEach
  void setUp() {
    getSrsCertificateFromWebcert = new GetSrsCertificateFromWebcert(intygModuleFacade, intygService);
  }

  @Test
  void shouldReturnNullWhenCertificateIdIsNull() {
    final var result = getSrsCertificateFromWebcert.getSrsCertificate(null);

    assertNull(result);
  }

  @Test
  void shouldReturnNullWhenCertificateIdIsEmpty() {
    final var result = getSrsCertificateFromWebcert.getSrsCertificate("");

    assertNull(result);
  }

  @Test
  void shouldReturnNullWhenCertificateIdIsBlank() {
    final var result = getSrsCertificateFromWebcert.getSrsCertificate("   ");

    assertNull(result);
  }

  @Test
  void shouldReturnNullWhenCertificateNotFound() {
    when(intygService.fetchIntygDataWithRelations("cert-1", LisjpEntryPoint.MODULE_ID)).thenReturn(null);

    final var result = getSrsCertificateFromWebcert.getSrsCertificate("cert-1");

    assertNull(result);
    verify(intygService).fetchIntygDataWithRelations("cert-1", LisjpEntryPoint.MODULE_ID);
  }

  @Test
  void shouldReturnNullWhenCertificateContentIsNull() {
    final var contentHolder = IntygContentHolder.builder()
        .contents(null)
        .build();

    when(intygService.fetchIntygDataWithRelations("cert-1", LisjpEntryPoint.MODULE_ID)).thenReturn(contentHolder);

    final var result = getSrsCertificateFromWebcert.getSrsCertificate("cert-1");

    assertNull(result);
  }

  @Test
  void shouldReturnSrsCertificateWithBasicInfo() {
    final var contentHolder = IntygContentHolder.builder()
        .contents("model-content")
        .build();

    final var utlatande = createBasicUtlatande("cert-1");

    when(intygService.fetchIntygDataWithRelations("cert-1", LisjpEntryPoint.MODULE_ID)).thenReturn(contentHolder);
    when(intygModuleFacade.getUtlatandeFromInternalModel(LisjpEntryPoint.MODULE_ID, "model-content"))
        .thenReturn(utlatande);

    final var result = getSrsCertificateFromWebcert.getSrsCertificate("cert-1");

    assertNotNull(result);
    assertEquals("cert-1", result.getCertificateId());
  }

  @Test
  void shouldSetSignedDateWhenAvailable() {
    final var contentHolder = IntygContentHolder.builder()
        .contents("model-content")
        .build();

    LocalDateTime signingDateTime = LocalDateTime.of(2025, 10, 30, 14, 30);
    final var utlatande = createBasicUtlatande("cert-1");
    utlatande.getGrundData().setSigneringsdatum(signingDateTime);

    when(intygService.fetchIntygDataWithRelations("cert-1", LisjpEntryPoint.MODULE_ID)).thenReturn(contentHolder);
    when(intygModuleFacade.getUtlatandeFromInternalModel(LisjpEntryPoint.MODULE_ID, "model-content"))
        .thenReturn(utlatande);

    final var result = getSrsCertificateFromWebcert.getSrsCertificate("cert-1");

    assertNotNull(result);
    assertEquals(signingDateTime.toLocalDate(), result.getSignedDate());
  }

  @Test
  void shouldSetMainDiagnosisCodeWhenAvailable() {
    final var contentHolder = IntygContentHolder.builder()
        .contents("model-content")
        .build();

    final var diagnoser = new ArrayList<Diagnos>();
    diagnoser.add(Diagnos.create("F438A", "ICD_10_SE", "Description", "Display"));

    final var utlatande = createUtlatandeWithDiagnoses("cert-1", diagnoser);

    when(intygService.fetchIntygDataWithRelations("cert-1", LisjpEntryPoint.MODULE_ID)).thenReturn(contentHolder);
    when(intygModuleFacade.getUtlatandeFromInternalModel(LisjpEntryPoint.MODULE_ID, "model-content"))
        .thenReturn(utlatande);

    final var result = getSrsCertificateFromWebcert.getSrsCertificate("cert-1");

    assertNotNull(result);
    assertEquals("F438A", result.getMainDiagnosisCode());
  }

  @Test
  void shouldSetExtendsCertificateIdWhenRelationIsFRLANG() {
    final var contentHolder = IntygContentHolder.builder()
        .contents("model-content")
        .build();

    final var utlatande = createBasicUtlatande("cert-1");
    final var relation = new Relation();
    relation.setRelationKod(RelationKod.FRLANG);
    relation.setRelationIntygsId("parent-cert-1");
    utlatande.getGrundData().setRelation(relation);

    when(intygService.fetchIntygDataWithRelations("cert-1", LisjpEntryPoint.MODULE_ID)).thenReturn(contentHolder);
    when(intygModuleFacade.getUtlatandeFromInternalModel(LisjpEntryPoint.MODULE_ID, "model-content"))
        .thenReturn(utlatande);

    final var result = getSrsCertificateFromWebcert.getSrsCertificate("cert-1");

    assertNotNull(result);
    assertEquals("parent-cert-1", result.getExtendsCertificateId());
  }

  @Test
  void shouldNotSetExtendsCertificateIdWhenRelationIsNotFRLANG() {
    final var contentHolder = IntygContentHolder.builder()
        .contents("model-content")
        .build();

    final var utlatande = createBasicUtlatande("cert-1");
    final var relation = new Relation();
    relation.setRelationKod(RelationKod.ERSATT);
    relation.setRelationIntygsId("parent-cert-1");
    utlatande.getGrundData().setRelation(relation);

    when(intygService.fetchIntygDataWithRelations("cert-1", LisjpEntryPoint.MODULE_ID)).thenReturn(contentHolder);
    when(intygModuleFacade.getUtlatandeFromInternalModel(LisjpEntryPoint.MODULE_ID, "model-content"))
        .thenReturn(utlatande);

    final var result = getSrsCertificateFromWebcert.getSrsCertificate("cert-1");

    assertNotNull(result);
    assertNull(result.getExtendsCertificateId());
  }

  @Test
  void shouldNotSetExtendsCertificateIdWhenRelationIntygsIdIsEmpty() {
    final var contentHolder = IntygContentHolder.builder()
        .contents("model-content")
        .build();

    final var utlatande = createBasicUtlatande("cert-1");
    final var relation = new Relation();
    relation.setRelationKod(RelationKod.FRLANG);
    relation.setRelationIntygsId("");
    utlatande.getGrundData().setRelation(relation);

    when(intygService.fetchIntygDataWithRelations("cert-1", LisjpEntryPoint.MODULE_ID)).thenReturn(contentHolder);
    when(intygModuleFacade.getUtlatandeFromInternalModel(LisjpEntryPoint.MODULE_ID, "model-content"))
        .thenReturn(utlatande);

    final var result = getSrsCertificateFromWebcert.getSrsCertificate("cert-1");

    assertNotNull(result);
    assertNull(result.getExtendsCertificateId());
  }

  @Test
  void shouldReturnCompletelyPopulatedSrsCertificate() {
    final var contentHolder = IntygContentHolder.builder()
        .contents("model-content")
        .build();

    final var signingDateTime = LocalDateTime.of(2025, 10, 30, 14, 30);

    final var diagnoser = new ArrayList<Diagnos>();
    diagnoser.add(Diagnos.create("F438A", "ICD_10_SE", "Description", "Display"));
    diagnoser.add(Diagnos.create("F439", "ICD_10_SE", "Description2", "Display2"));

    final var utlatande = createUtlatandeWithDiagnoses("cert-1", diagnoser);
    utlatande.getGrundData().setSigneringsdatum(signingDateTime);

    final var relation = new Relation();
    relation.setRelationKod(RelationKod.FRLANG);
    relation.setRelationIntygsId("parent-cert-1");
    utlatande.getGrundData().setRelation(relation);

    when(intygService.fetchIntygDataWithRelations("cert-1", LisjpEntryPoint.MODULE_ID)).thenReturn(contentHolder);
    when(intygModuleFacade.getUtlatandeFromInternalModel(LisjpEntryPoint.MODULE_ID, "model-content"))
        .thenReturn(utlatande);

    final var result = getSrsCertificateFromWebcert.getSrsCertificate("cert-1");

    assertNotNull(result);
    assertEquals("cert-1", result.getCertificateId());
    assertEquals("F438A", result.getMainDiagnosisCode());
    assertEquals(signingDateTime.toLocalDate(), result.getSignedDate());
    assertEquals("parent-cert-1", result.getExtendsCertificateId());
  }

  @Test
  void shouldHandleUtlatandeWithoutGrundData() {
    final var contentHolder = IntygContentHolder.builder()
        .contents("model-content")
        .build();

    final var utlatande = LisjpUtlatandeV1.builder()
        .setId("cert-1")
        .setGrundData(new GrundData())
        .setTextVersion("1.0")
        .build();

    when(intygService.fetchIntygDataWithRelations("cert-1", LisjpEntryPoint.MODULE_ID)).thenReturn(contentHolder);
    when(intygModuleFacade.getUtlatandeFromInternalModel(LisjpEntryPoint.MODULE_ID, "model-content"))
        .thenReturn(utlatande);

    final var result = getSrsCertificateFromWebcert.getSrsCertificate("cert-1");

    assertNotNull(result);
    assertEquals("cert-1", result.getCertificateId());
    assertNull(result.getSignedDate());
    assertNull(result.getExtendsCertificateId());
  }

  @Test
  void shouldHandleEmptyDiagnosisList() {
    final var contentHolder = IntygContentHolder.builder()
        .contents("model-content")
        .build();

    final var utlatande = createUtlatandeWithDiagnoses("cert-1", new ArrayList<>());

    when(intygService.fetchIntygDataWithRelations("cert-1", LisjpEntryPoint.MODULE_ID)).thenReturn(contentHolder);
    when(intygModuleFacade.getUtlatandeFromInternalModel(LisjpEntryPoint.MODULE_ID, "model-content"))
        .thenReturn(utlatande);

    final var result = getSrsCertificateFromWebcert.getSrsCertificate("cert-1");

    assertNotNull(result);
    assertNull(result.getMainDiagnosisCode());
  }

  private LisjpUtlatandeV1 createBasicUtlatande(String id) {
    final var grundData = new GrundData();

    return LisjpUtlatandeV1.builder()
        .setId(id)
        .setGrundData(grundData)
        .setTextVersion("1.0")
        .build();
  }

  private LisjpUtlatandeV1 createUtlatandeWithDiagnoses(String id, List<Diagnos> diagnoses) {
    final var grundData = new GrundData();

    return LisjpUtlatandeV1.builder()
        .setId(id)
        .setGrundData(grundData)
        .setDiagnoser(diagnoses)
        .setTextVersion("1.0")
        .build();
  }

}


