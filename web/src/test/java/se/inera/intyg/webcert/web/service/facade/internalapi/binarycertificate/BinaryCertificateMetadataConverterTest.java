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
package se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static se.inera.intyg.common.support.Constants.KV_INTYGSTYP_CODE_SYSTEM;
import static se.inera.intyg.common.support.Constants.KV_UTLATANDETYP_INTYG_CODE_SYSTEM;
import static se.inera.intyg.common.support.facade.model.CertificateRelationType.COMPLEMENTED;
import static se.inera.intyg.common.support.model.CertificateState.CANCELLED;
import static se.inera.intyg.common.support.model.CertificateState.RECEIVED;
import static se.inera.intyg.common.support.model.CertificateState.SENT;
import static se.inera.intyg.webcert.common.dto.PersonIdType.COORDINATION_NUMBER;
import static se.inera.intyg.webcert.common.dto.PersonIdType.PERSONAL_IDENTITY_NUMBER;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.af00213.support.Af00213EntryPoint;
import se.inera.intyg.common.ag114.support.Ag114EntryPoint;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.luae_fs.support.LuaefsEntryPoint;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.PaTitle;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.BinaryCertificateMetadataDTO;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

@ExtendWith(MockitoExtension.class)
class BinaryCertificateMetadataConverterTest {

  @Mock private Utlatande utlatande;

  @Mock private Utlatande parentUtlatande;

  @InjectMocks private BinaryCertificateMetadataConverter converter;

  private static final String UNIT_ID = "unit-id";
  private static final String UNIT_NAME = "unit-name";
  private static final String UNIT_ADDRESS = "Adressgatan 1";
  private static final String UNIT_ZIP_CODE = "54321";
  private static final String UNIT_CITY = "Flen";
  private static final String UNIT_WORKPLACE_CODE = "717171";
  private static final String UNIT_MAIL_ADDRESS = "example@example.se";
  private static final String UNIT_PHONE_NUMBER = "0311234567";
  private static final String CARE_PROVIDER_ID = "care-provider-id";
  private static final String CARE_PROVIDER_NAME = "care-provider-name";
  private static final String PRESCRIPTION_CODE = "0000000";
  private static final String SURGERY = "Kirurgi";
  private static final String OFTALMOLOGY = "Oftalmologi";
  private static final String HSA_ID = "SE2321000077-1234";
  private static final String PA_CODE = "204010";
  private static final String PA_TEXT = "Läkare ej legitimerad, allmäntjänstgöring";
  private static final String PARENT_UNIT_ID = "PARENT_UNIT_ID";
  private static final String CERTIFICATE_ID = "intyg-id";
  private static final String PARENT_CERTIFICATE_ID = "parentCertificateId";
  private static final String CERTIFICATE_VERSION = "1.0";
  private static final String PERSON_ID = "191212121212";
  private static final String COORDINATION_ID = "191212621212";
  private static final String RECIPIENT_ID = "FKASSA";
  private static final String FULL_NAME = "Ajla Doktor";
  private static final LocalDateTime RECEIVED_AT = LocalDateTime.parse("2026-01-01T10:00:00");
  private static final LocalDateTime SENT_AT = LocalDateTime.parse("2026-01-02T11:00:00");
  private static final LocalDateTime CANCELLED_AT = LocalDateTime.parse("2026-01-03T12:00:00");

  private IntygContentHolder content;
  private final ModuleEntryPoint entryPoint = new LisjpEntryPoint();

  @BeforeEach
  void setUp() {
    final var parentRelation = new WebcertCertificateRelation();
    final var relations = new Relations();
    parentRelation.setRelationKod(RelationKod.KOMPLT);
    parentRelation.setIntygsId(PARENT_CERTIFICATE_ID);
    relations.setParent(parentRelation);
    content =
        IntygContentHolder.builder()
            .utlatande(utlatande)
            .statuses(
                List.of(
                    new Status(RECEIVED, null, RECEIVED_AT),
                    new Status(SENT, RECIPIENT_ID, SENT_AT),
                    new Status(CANCELLED, null, CANCELLED_AT)))
            .relations(relations)
            .build();

    when(utlatande.getGrundData()).thenReturn(getGrundData(PERSON_ID, UNIT_ID));
  }

  @Test
  void shouldMapCertificateId() {
    when(utlatande.getId()).thenReturn(CERTIFICATE_ID);
    assertEquals(CERTIFICATE_ID, convert(content, entryPoint).getCertificateId());
  }

  @ParameterizedTest
  @MethodSource("entryPoints")
  void shouldMapTypeFromEntryPoint(
      ModuleEntryPoint entryPoint,
      String expectedExternalId,
      String expectedModuleName,
      String expectedCodeSystem) {

    final var result = convert(content, entryPoint);

    assertAll(
        () -> assertEquals(expectedExternalId, result.getType().getCode()),
        () -> assertEquals(expectedModuleName, result.getType().getDisplayName()),
        () -> assertEquals(expectedCodeSystem, result.getType().getCodeSystem()));
  }

  @Test
  void shouldMapVersion() {
    when(utlatande.getTextVersion()).thenReturn(CERTIFICATE_VERSION);
    assertEquals(CERTIFICATE_VERSION, convert(content, entryPoint).getVersion());
  }

  @Test
  void shouldMapPatient() {
    final var patient = convert(content, entryPoint).getPatient();
    assertAll(
        () -> assertEquals(PERSON_ID, patient.getPatientId()),
        () -> assertEquals(PERSONAL_IDENTITY_NUMBER, patient.getType()));
  }

  @Test
  void shouldMapPatientWithCoordinationNumber() {
    when(utlatande.getGrundData()).thenReturn(getGrundData(COORDINATION_ID, UNIT_ID));

    final var patient = convert(content, entryPoint).getPatient();

    assertAll(
        () -> assertEquals(COORDINATION_ID, patient.getPatientId()),
        () -> assertEquals(COORDINATION_NUMBER, patient.getType()));
  }

  @Test
  void shouldMapIssuedBy() {
    final var issuedBy = convert(content, entryPoint).getIssuedBy();

    assertAll(
        () -> assertEquals(HSA_ID, issuedBy.getPersonId()),
        () -> assertEquals(FULL_NAME, issuedBy.getFullName()),
        () -> assertEquals(PA_CODE, issuedBy.getTitles().getFirst().getCode()),
        () -> assertEquals(PA_TEXT, issuedBy.getTitles().getFirst().getDisplayName()),
        () -> assertIterableEquals(Collections.emptyList(), issuedBy.getLicences()),
        () -> assertIterableEquals(List.of(SURGERY, OFTALMOLOGY), issuedBy.getSpecialities()));
  }

  @Test
  void shouldMapIssuedByUnit() {
    final var unit = convert(content, entryPoint).getIssuedBy().getUnit();

    assertAll(
        () -> assertEquals(UNIT_ID, unit.getUnitId()),
        () -> assertEquals(UNIT_NAME, unit.getUnitName()),
        () -> assertEquals(UNIT_ADDRESS, unit.getAddress()),
        () -> assertEquals(UNIT_ZIP_CODE, unit.getZipCode()),
        () -> assertEquals(UNIT_CITY, unit.getCity()),
        () -> assertEquals(UNIT_MAIL_ADDRESS, unit.getEmail()),
        () -> assertEquals(UNIT_PHONE_NUMBER, unit.getPhoneNumber()),
        () -> assertEquals(UNIT_WORKPLACE_CODE, unit.getWorkplaceCode()),
        () -> assertEquals(CARE_PROVIDER_ID, unit.getCareProvider().getUnitId()),
        () -> assertEquals(CARE_PROVIDER_NAME, unit.getCareProvider().getUnitName()));
  }

  @Test
  void shouldMapSignedAtFromReceivedStatus() {
    assertEquals(RECEIVED_AT, convert(content, entryPoint).getSignedAt());
  }

  @Test
  void shouldMapSentAtFromSentStatus() {
    assertEquals(SENT_AT, convert(content, entryPoint).getSentAt());
  }

  @Test
  void shouldMapRecipientIdFromSentStatus() {
    assertEquals(RECIPIENT_ID, convert(content, entryPoint).getRecipientId());
  }

  @Test
  void shouldMapRevokedAtFromCancelledStatus() {
    assertEquals(CANCELLED_AT, convert(content, entryPoint).getRevokedAt());
  }

  @Test
  void shouldSetNullWhenSentStatusIsMissing() {
    content =
        IntygContentHolder.builder()
            .utlatande(utlatande)
            .statuses(List.of(new Status(RECEIVED, null, RECEIVED_AT)))
            .build();

    assertNull(convert(content, entryPoint).getSentAt());
  }

  @Test
  void shouldSetNullWhenCancelledStatusIsMissing() {
    content =
        IntygContentHolder.builder()
            .utlatande(utlatande)
            .statuses(
                List.of(
                    new Status(RECEIVED, null, RECEIVED_AT),
                    new Status(SENT, RECIPIENT_ID, SENT_AT)))
            .build();

    assertNull(convert(content, entryPoint).getRevokedAt());
  }

  @Test
  void shouldMapParentRelationWhenParentCertificateExists() {
    final var parentCertificate = IntygContentHolder.builder().utlatande(parentUtlatande).build();
    when(parentUtlatande.getGrundData()).thenReturn(getGrundData(PERSON_ID, PARENT_UNIT_ID));

    final var parentRelation = convert(content, entryPoint, parentCertificate).getParentRelation();

    assertAll(
        () -> assertEquals(PARENT_CERTIFICATE_ID, parentRelation.getCertificateId()),
        () -> assertEquals(COMPLEMENTED, parentRelation.getType()),
        () -> assertEquals(PARENT_UNIT_ID, parentRelation.getIssuingUnitId()));
  }

  @Test
  void shouldSetParentRelationToNullWhenParentCertificateIsMissing() {
    assertNull(convert(content, entryPoint).getParentRelation());
  }

  private BinaryCertificateMetadataDTO convert(
      IntygContentHolder content, ModuleEntryPoint entryPoint) {
    return convert(content, entryPoint, null);
  }

  private BinaryCertificateMetadataDTO convert(
      IntygContentHolder content,
      ModuleEntryPoint entryPoint,
      IntygContentHolder parentCertificate) {
    return converter.toBinaryCertificate(content, entryPoint, parentCertificate);
  }

  private static GrundData getGrundData(String patientId, String unitId) {
    final var patient = new Patient();
    final var skapadAv = new HoSPersonal();
    final var grundData = new GrundData();
    final var paTitle = new PaTitle();
    paTitle.setKod(PA_CODE);
    paTitle.setKlartext(PA_TEXT);
    patient.setPersonId(Personnummer.createPersonnummer(patientId).orElseThrow());
    grundData.setPatient(patient);
    skapadAv.setPersonId(HSA_ID);
    skapadAv.setFullstandigtNamn(FULL_NAME);
    skapadAv.setVardenhet(unit(unitId));
    skapadAv.setForskrivarKod(PRESCRIPTION_CODE);
    skapadAv.getSpecialiteter().addAll(List.of(SURGERY, OFTALMOLOGY));
    skapadAv.getBefattningsKoder().add(paTitle);
    grundData.setSkapadAv(skapadAv);
    return grundData;
  }

  private static Vardenhet unit(String unitId) {
    final var unit = new Vardenhet();
    unit.setEnhetsid(unitId);
    unit.setEnhetsnamn(UNIT_NAME);
    unit.setPostadress(UNIT_ADDRESS);
    unit.setPostnummer(UNIT_ZIP_CODE);
    unit.setPostort(UNIT_CITY);
    unit.setArbetsplatsKod(UNIT_WORKPLACE_CODE);
    unit.setEpost(UNIT_MAIL_ADDRESS);
    unit.setTelefonnummer(UNIT_PHONE_NUMBER);
    unit.setVardgivare(vardgivare());
    return unit;
  }

  private static Vardgivare vardgivare() {
    final var vardgivare = new Vardgivare();
    vardgivare.setVardgivarid(CARE_PROVIDER_ID);
    vardgivare.setVardgivarnamn(CARE_PROVIDER_NAME);
    return vardgivare;
  }

  private static Stream<Arguments> entryPoints() {
    return Stream.of(
        Arguments.of(new DbModuleEntryPoint(), "DB", "Dödsbevis", KV_INTYGSTYP_CODE_SYSTEM),
        Arguments.of(new DoiModuleEntryPoint(), "DOI", "Dödsorsaksintyg", KV_INTYGSTYP_CODE_SYSTEM),
        Arguments.of(
            new LisjpEntryPoint(), "LISJP", "Läkarintyg för sjukpenning", KV_INTYGSTYP_CODE_SYSTEM),
        Arguments.of(
            new Fk7263EntryPoint(),
            "FK7263",
            "Läkarintyg FK 7263",
            KV_UTLATANDETYP_INTYG_CODE_SYSTEM),
        Arguments.of(
            new LuseEntryPoint(),
            "LUSE",
            "Läkarutlåtande för sjukersättning",
            KV_INTYGSTYP_CODE_SYSTEM),
        Arguments.of(
            new Af00213EntryPoint(),
            "AF00213",
            "Arbetsförmedlingens medicinska utlåtande",
            KV_INTYGSTYP_CODE_SYSTEM),
        Arguments.of(
            new Ag7804EntryPoint(),
            "AG7804",
            "Läkarintyg om arbetsförmåga – arbetsgivaren",
            KV_INTYGSTYP_CODE_SYSTEM),
        Arguments.of(
            new Ag114EntryPoint(),
            "AG1-14",
            "Läkarintyg om arbetsförmåga – sjuklöneperioden",
            KV_INTYGSTYP_CODE_SYSTEM),
        Arguments.of(
            new TsDiabetesEntryPoint(),
            "TSTRK1031",
            "Transportstyrelsens läkarintyg diabetes",
            KV_UTLATANDETYP_INTYG_CODE_SYSTEM),
        Arguments.of(
            new LuaefsEntryPoint(),
            "LUAE_FS",
            "Läkarutlåtande för aktivitetsersättning vid förlängd skolgång",
            KV_INTYGSTYP_CODE_SYSTEM),
        Arguments.of(
            new LuaenaEntryPoint(),
            "LUAE_NA",
            "Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga",
            KV_INTYGSTYP_CODE_SYSTEM),
        Arguments.of(
            new TsBasEntryPoint(),
            "TSTRK1007",
            "Transportstyrelsens läkarintyg högre körkortsbehörighet",
            KV_UTLATANDETYP_INTYG_CODE_SYSTEM));
  }
}
