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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.OrganizationUtil.DEFAULT_ARBETSPLATSKOD;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareUnitMember;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.AgandeForm;

@ExtendWith(MockitoExtension.class)
class CareUnitMemberConverterTest {

  private static final String ZIP_CODE = "ZIP_CODE";
  private static final String CITY = "CITY";
  private static final String ADDRESS = "ADDRESS";
  private static final String PARENT_ID = "PARENT_ID";
  private static final AgandeForm PARENT_AGANDE_FORM = AgandeForm.OFFENTLIG;

  @Mock private UnitAddressConverter unitAddressConverter;

  @InjectMocks private CareUnitMemberConverter converter;

  @Test
  void shouldConvertParentId() {
    final var member = getMember();

    final var response = converter.convert(member, PARENT_ID, PARENT_AGANDE_FORM);

    assertEquals(PARENT_ID, response.getParentHsaId());
  }

  @Test
  void shouldConvertAgandeForm() {
    final var member = getMember();

    final var response = converter.convert(member, PARENT_ID, PARENT_AGANDE_FORM);

    assertEquals(PARENT_AGANDE_FORM, response.getAgandeForm());
  }

  @Test
  void shouldConvertStart() {
    final var member = getMember();

    final var response = converter.convert(member, PARENT_ID, PARENT_AGANDE_FORM);

    assertEquals(member.getHealthCareUnitMemberStartDate(), response.getStart());
  }

  @Test
  void shouldConvertEnd() {
    final var member = getMember();

    final var response = converter.convert(member, PARENT_ID, PARENT_AGANDE_FORM);

    assertEquals(member.getHealthCareUnitMemberEndDate(), response.getEnd());
  }

  @Test
  void shouldConvertHsaId() {
    final var member = getMember();

    final var response = converter.convert(member, PARENT_ID, PARENT_AGANDE_FORM);

    assertEquals(member.getHealthCareUnitMemberHsaId(), response.getId());
  }

  @Test
  void shouldConvertName() {
    final var member = getMember();

    final var response = converter.convert(member, PARENT_ID, PARENT_AGANDE_FORM);

    assertEquals(member.getHealthCareUnitMemberName(), response.getNamn());
  }

  @Test
  void shouldConvertPhoneNumber() {
    final var member = getMember();

    final var response = converter.convert(member, PARENT_ID, PARENT_AGANDE_FORM);

    assertEquals("1, 2", response.getTelefonnummer());
  }

  @Nested
  class WorkPlaceCodeTest {

    @Test
    void shouldConvertToDefaultIfNoPrescriptionCode() {
      final var member = getMember();

      member.setHealthCareUnitMemberPrescriptionCode(Collections.emptyList());

      final var response = converter.convert(member, PARENT_ID, PARENT_AGANDE_FORM);

      assertEquals(DEFAULT_ARBETSPLATSKOD, response.getArbetsplatskod());
    }

    @Test
    void shouldConvertToDefaultIfNullPrescriptionCode() {
      final var member = getMember();

      member.setHealthCareUnitMemberPrescriptionCode(null);

      final var response = converter.convert(member, PARENT_ID, PARENT_AGANDE_FORM);

      assertEquals(DEFAULT_ARBETSPLATSKOD, response.getArbetsplatskod());
    }

    @Test
    void shouldConvertToFirstPrescriptionCode() {
      final var member = getMember();

      member.setHealthCareUnitMemberPrescriptionCode(List.of("CODE", "CODE2"));

      final var response = converter.convert(member, PARENT_ID, PARENT_AGANDE_FORM);

      assertEquals("CODE", response.getArbetsplatskod());
    }
  }

  @Nested
  class AddressTest {

    @BeforeEach
    void setup() {
      when(unitAddressConverter.convertAddress(any(List.class))).thenReturn(ADDRESS);

      when(unitAddressConverter.convertCity(any(List.class))).thenReturn(CITY);

      when(unitAddressConverter.convertZipCode(any(List.class), anyString())).thenReturn(ZIP_CODE);
    }

    @Test
    void shouldConvertAddress() {
      final var member = getMember();

      final var response = converter.convert(member, PARENT_ID, PARENT_AGANDE_FORM);

      verify(unitAddressConverter).convertAddress(member.getHealthCareUnitMemberpostalAddress());
      assertEquals(ADDRESS, response.getPostadress());
    }

    @Test
    void shouldConvertCity() {
      final var member = getMember();

      final var response = converter.convert(member, PARENT_ID, PARENT_AGANDE_FORM);

      verify(unitAddressConverter).convertCity(member.getHealthCareUnitMemberpostalAddress());
      assertEquals(CITY, response.getPostort());
    }

    @Test
    void shouldConvertZipCode() {
      final var member = getMember();

      final var response = converter.convert(member, PARENT_ID, PARENT_AGANDE_FORM);

      verify(unitAddressConverter)
          .convertZipCode(
              member.getHealthCareUnitMemberpostalAddress(),
              member.getHealthCareUnitMemberpostalCode());
      assertEquals(ZIP_CODE, response.getPostnummer());
    }
  }

  private HealthCareUnitMember getMember() {
    final var member = new HealthCareUnitMember();
    member.setHealthCareUnitMemberHsaId("ID");
    member.setHealthCareUnitMemberName("NAME");
    member.setHealthCareUnitMemberStartDate(LocalDateTime.now());
    member.setHealthCareUnitMemberEndDate(LocalDateTime.now().plusDays(5));
    member.setHealthCareUnitMemberTelephoneNumber(List.of("1", "2"));
    member.setHealthCareUnitMemberpostalAddress(Collections.emptyList());
    member.setHealthCareUnitMemberpostalCode("ZIP");

    return member;
  }
}
