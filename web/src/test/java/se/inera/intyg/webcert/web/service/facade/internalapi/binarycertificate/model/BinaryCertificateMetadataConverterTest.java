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
package se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.webcert.common.dto.PersonIdType.PERSONAL_IDENTITY_NUMBER;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.BinaryCertificateMetadataConverter;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Befattning;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.LegitimeratYrkeType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Specialistkompetens;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

class BinaryCertificateMetadataConverterTest {

  private static final String CERTIFICATE_ID = "e5f6a1b2-3c4d-4e5f-8a1b-2c3d4e5f6a1b";
  private static final String CERTIFICATE_TYPE = "lisjp";
  private static final String CERTIFICATE_TYPE_NAME = "Läkarintyg för sjukpenning";
  private static final String CERTIFICATE_TYPE_VERSION = "1.3";
  private static final String CERTIFICATE_TYPE_CODE_SYSTEM = "b64ea353-e8f6-4832-b563-fc7d46f29548";
  private static final String INTYG_TYPE_CODE = "LISJP";
  private static final String INTYG_TYPE_DISPLAY_NAME = "Intyg från Intyg";
  private static final LocalDateTime SIGNED_AT = LocalDateTime.of(2026, 1, 2, 10, 0);
  private static final LocalDateTime REVOKED_AT = LocalDateTime.of(2026, 1, 4, 12, 0);
  private static final LocalDateTime SENT_AT = LocalDateTime.of(2026, 1, 3, 11, 0);
  private static final String PATIENT_ID = "191212121212";
  private static final Patient PATIENT =
      Patient.builder().personId(PersonId.builder().id(PATIENT_ID).build()).build();
  private static final CertificateRelations RELATIONS = CertificateRelations.builder().build();

  private static final String STAFF_PERSON_ID = "TSTNMT2321000156-1079";
  private static final String STAFF_FULL_NAME = "Ajla Doktor";
  private static final String INTYG_STAFF_PERSON_ID = "TSTNMT2321000156-XXXX";
  private static final String INTYG_STAFF_FULL_NAME = "Beata Doktor";

  private static final String TITLE_CODE = "204010";
  private static final String TITLE_CODE_SYSTEM = "1.2.752.129.2.2.1.4";
  private static final String TITLE_DISPLAY_NAME = "Läkare";
  private static final String LICENCE_CODE = "LK";
  private static final String LICENCE_CODE_SYSTEM = "1.2.752.129.2.2.1.5";
  private static final String LICENCE_DISPLAY_NAME = "Läkare legitimation";
  private static final String SPECIALITY_CODE = "1";
  private static final String SPECIALITY_DISPLAY_NAME = "Allmänmedicin";

  private static final String UNIT_ID = "TSTNMT2321000156-ALMC";
  private static final String UNIT_NAME = "Alfa Medicincentrum";
  private static final String UNIT_ADDRESS = "Vardagsgatan 1";
  private static final String UNIT_ZIP_CODE = "12345";
  private static final String UNIT_CITY = "Småstad";
  private static final String UNIT_PHONE_NUMBER = "0812345678";
  private static final String UNIT_WORKPLACE_CODE = "1234567890";
  private static final String UNIT_EMAIL = "alfa@medicincentrum.se";
  private static final String CARE_PROVIDER_ID = "TSTNMT2321000156-ALFA";
  private static final String CARE_PROVIDER_NAME = "Alfa Regionen";
  private static final String CARE_UNIT_ID = "TSTNMT2321000156-ALMI";
  private static final String CARE_UNIT_NAME = "Alfa Mottagningen";

  private final BinaryCertificateMetadataConverter converter =
      new BinaryCertificateMetadataConverter();

  private Intyg intyg;
  private Certificate certificate;

  @BeforeEach
  void setUp() {
    intyg = intyg();
    certificate = certificate();
  }

  private static Intyg intyg() {
    final var typ = new TypAvIntyg();
    typ.setCode(INTYG_TYPE_CODE);
    typ.setCodeSystem(CERTIFICATE_TYPE_CODE_SYSTEM);
    typ.setDisplayName(INTYG_TYPE_DISPLAY_NAME);

    final var befattning = new Befattning();
    befattning.setCode(TITLE_CODE);
    befattning.setCodeSystem(TITLE_CODE_SYSTEM);
    befattning.setDisplayName(TITLE_DISPLAY_NAME);

    final var legitimeratYrke = new LegitimeratYrkeType();
    legitimeratYrke.setCode(LICENCE_CODE);
    legitimeratYrke.setCodeSystem(LICENCE_CODE_SYSTEM);
    legitimeratYrke.setDisplayName(LICENCE_DISPLAY_NAME);

    final var specialistkompetens = new Specialistkompetens();
    specialistkompetens.setCode(SPECIALITY_CODE);
    specialistkompetens.setDisplayName(SPECIALITY_DISPLAY_NAME);

    final var skapadAv = new HosPersonal();
    skapadAv.setFullstandigtNamn(INTYG_STAFF_FULL_NAME);
    skapadAv.getBefattning().add(befattning);
    skapadAv.getLegitimeratYrke().add(legitimeratYrke);
    skapadAv.getSpecialistkompetens().add(specialistkompetens);

    final var intyg = new Intyg();
    intyg.setTyp(typ);
    intyg.setVersion("intygVersion");
    intyg.setSigneringstidpunkt(LocalDateTime.of(2020, 1, 1, 0, 0));
    intyg.setSkickatTidpunkt(SENT_AT);
    intyg.setSkapadAv(skapadAv);
    return intyg;
  }

  private static Certificate certificate() {
    final var certificate = new Certificate();
    certificate.setMetadata(metadataBuilder().build());
    return certificate;
  }

  private static CertificateMetadata.CertificateMetadataBuilder metadataBuilder() {
    return CertificateMetadata.builder()
        .id(CERTIFICATE_ID)
        .type(CERTIFICATE_TYPE)
        .typeName(CERTIFICATE_TYPE_NAME)
        .typeVersion(CERTIFICATE_TYPE_VERSION)
        .signed(SIGNED_AT)
        .revokedAt(REVOKED_AT)
        .patient(PATIENT)
        .relations(RELATIONS)
        .issuedBy(Staff.builder().personId(STAFF_PERSON_ID).fullName(STAFF_FULL_NAME).build())
        .unit(
            Unit.builder()
                .unitId(UNIT_ID)
                .unitName(UNIT_NAME)
                .address(UNIT_ADDRESS)
                .zipCode(UNIT_ZIP_CODE)
                .city(UNIT_CITY)
                .phoneNumber(UNIT_PHONE_NUMBER)
                .workplaceCode(UNIT_WORKPLACE_CODE)
                .email(UNIT_EMAIL)
                .build())
        .careUnit(Unit.builder().unitId(CARE_UNIT_ID).unitName(CARE_UNIT_NAME).build())
        .careProvider(Unit.builder().unitId(CARE_PROVIDER_ID).unitName(CARE_PROVIDER_NAME).build());
  }

  private void setMetadata(CertificateMetadata metadata) {
    certificate.setMetadata(metadata);
  }

  private BinaryCertificateMetadataDTO convert() {
    return converter.toBinaryCertificate(intyg, certificate);
  }

  @Nested
  class CertificateId {

    @Test
    void shouldSetCertificateIdFromCertificate() {
      assertEquals(CERTIFICATE_ID, convert().getCertificateId());
    }
  }

  @Nested
  class Type {

    @Test
    void shouldSetTypeCodeFromCertificate() {
      assertEquals(CERTIFICATE_TYPE, convert().getType().getCode());
    }

    @Test
    void shouldSetTypeCodeSystemFromIntyg() {
      assertEquals(CERTIFICATE_TYPE_CODE_SYSTEM, convert().getType().getCodeSystem());
    }

    @Test
    void shouldSetTypeDisplayNameFromCertificate() {
      assertEquals(CERTIFICATE_TYPE_NAME, convert().getType().getDisplayName());
    }
  }

  @Nested
  class Version {

    @Test
    void shouldSetVersionFromCertificate() {
      assertEquals(CERTIFICATE_TYPE_VERSION, convert().getVersion());
    }
  }

  @Nested
  class Timestamps {

    @Test
    void shouldSetSignedAtFromCertificate() {
      assertEquals(SIGNED_AT, convert().getSignedAt());
    }

    @Test
    void shouldSetRevokedAtFromCertificate() {
      assertEquals(REVOKED_AT, convert().getRevokedAt());
    }

    @Test
    void shouldSetSentAtFromIntyg() {
      assertEquals(SENT_AT, convert().getSentAt());
    }

    @Test
    void shouldSetSentAtToNullWhenMissingInIntyg() {
      intyg.setSkickatTidpunkt(null);

      assertNull(convert().getSentAt());
    }

    @Test
    void shouldSetRevokedAtToNullWhenMissingInCertificate() {
      setMetadata(metadataBuilder().revokedAt(null).build());

      assertNull(convert().getRevokedAt());
    }
  }

  @Nested
  class PatientTest {

    @Test
    void shouldSetPatientIdFromCertificate() {
      assertEquals(PATIENT_ID, convert().getPatient().getPatientId());
    }

    @Test
    void shouldSetPatientIdTypeFromCertificate() {
      assertEquals(PERSONAL_IDENTITY_NUMBER, convert().getPatient().getType());
    }
  }

  @Nested
  class Relations {

    @Test
    void shouldSetRelationsFromCertificate() {
      assertEquals(RELATIONS, convert().getRelations());
    }
  }

  @Nested
  class IssuedBy {

    @Test
    void shouldSetPersonIdFromCertificate() {
      assertEquals(STAFF_PERSON_ID, convert().getIssuedBy().getPersonId());
    }

    @Test
    void shouldSetFullNameFromCertificate() {
      assertEquals(STAFF_FULL_NAME, convert().getIssuedBy().getFullName());
    }

    @Test
    void shouldNotSetFullNameFromIntyg() {
      setMetadata(
          metadataBuilder()
              .issuedBy(Staff.builder().personId(INTYG_STAFF_PERSON_ID).build())
              .build());

      assertNull(convert().getIssuedBy().getFullName());
    }
  }

  @Nested
  class Titles {

    @Test
    void shouldSetOneTitleFromIntyg() {
      assertEquals(1, convert().getIssuedBy().getTitles().size());
    }

    @Test
    void shouldSetTitleCodeFromIntyg() {
      assertEquals(TITLE_CODE, convert().getIssuedBy().getTitles().getFirst().getCode());
    }

    @Test
    void shouldSetTitleCodeSystemFromIntyg() {
      assertEquals(
          TITLE_CODE_SYSTEM, convert().getIssuedBy().getTitles().getFirst().getCodeSystem());
    }

    @Test
    void shouldSetTitleDisplayNameFromIntyg() {
      assertEquals(
          TITLE_DISPLAY_NAME, convert().getIssuedBy().getTitles().getFirst().getDisplayName());
    }

    @Test
    void shouldSetEmptyTitlesWhenMissingInIntyg() {
      intyg.getSkapadAv().getBefattning().clear();

      assertTrue(convert().getIssuedBy().getTitles().isEmpty());
    }
  }

  @Nested
  class Licences {

    @Test
    void shouldSetOneLicenceFromIntyg() {
      assertEquals(1, convert().getIssuedBy().getLicences().size());
    }

    @Test
    void shouldSetLicenceCodeFromIntyg() {
      assertEquals(LICENCE_CODE, convert().getIssuedBy().getLicences().getFirst().getCode());
    }

    @Test
    void shouldSetLicenceCodeSystemFromIntyg() {
      assertEquals(
          LICENCE_CODE_SYSTEM, convert().getIssuedBy().getLicences().getFirst().getCodeSystem());
    }

    @Test
    void shouldSetLicenceDisplayNameFromIntyg() {
      assertEquals(
          LICENCE_DISPLAY_NAME, convert().getIssuedBy().getLicences().getFirst().getDisplayName());
    }

    @Test
    void shouldSetEmptyLicencesWhenMissingInIntyg() {
      intyg.getSkapadAv().getLegitimeratYrke().clear();

      assertTrue(convert().getIssuedBy().getLicences().isEmpty());
    }
  }

  @Nested
  class Specialities {

    @Test
    void shouldSetSpecialitiesFromIntygDisplayName() {
      assertEquals(List.of(SPECIALITY_DISPLAY_NAME), convert().getIssuedBy().getSpecialities());
    }

    @Test
    void shouldSetEmptySpecialitiesWhenMissingInIntyg() {
      intyg.getSkapadAv().getSpecialistkompetens().clear();

      assertTrue(convert().getIssuedBy().getSpecialities().isEmpty());
    }
  }

  @Nested
  class UnitTest {

    @Test
    void shouldSetUnitIdFromCertificate() {
      assertEquals(UNIT_ID, convert().getIssuedBy().getUnit().getUnitId());
    }

    @Test
    void shouldSetUnitNameFromCertificate() {
      assertEquals(UNIT_NAME, convert().getIssuedBy().getUnit().getUnitName());
    }

    @Test
    void shouldSetAddressFromCertificate() {
      assertEquals(UNIT_ADDRESS, convert().getIssuedBy().getUnit().getAddress());
    }

    @Test
    void shouldSetZipCodeFromCertificate() {
      assertEquals(UNIT_ZIP_CODE, convert().getIssuedBy().getUnit().getZipCode());
    }

    @Test
    void shouldSetCityFromCertificate() {
      assertEquals(UNIT_CITY, convert().getIssuedBy().getUnit().getCity());
    }

    @Test
    void shouldSetPhoneNumberFromCertificate() {
      assertEquals(UNIT_PHONE_NUMBER, convert().getIssuedBy().getUnit().getPhoneNumber());
    }

    @Test
    void shouldSetWorkplaceCodeFromCertificate() {
      assertEquals(UNIT_WORKPLACE_CODE, convert().getIssuedBy().getUnit().getWorkplaceCode());
    }

    @Test
    void shouldSetEmailFromCertificate() {
      assertEquals(UNIT_EMAIL, convert().getIssuedBy().getUnit().getEmail());
    }
  }

  @Nested
  class CareProvider {

    @Test
    void shouldSetCareProviderIdFromCertificate() {
      assertEquals(
          CARE_PROVIDER_ID, convert().getIssuedBy().getUnit().getCareProvider().getUnitId());
    }

    @Test
    void shouldSetCareProviderNameFromCertificate() {
      assertEquals(
          CARE_PROVIDER_NAME, convert().getIssuedBy().getUnit().getCareProvider().getUnitName());
    }
  }
}
