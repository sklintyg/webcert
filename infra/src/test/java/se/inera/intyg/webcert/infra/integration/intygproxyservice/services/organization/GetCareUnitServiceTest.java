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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Commission;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareUnitMembers;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Unit;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareUnitMembersRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetUnitRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.converter.CareUnitConverter;

@ExtendWith(MockitoExtension.class)
class GetCareUnitServiceTest {

  private static final String ID = "ID";

  @Mock GetUnitService getUnitService;

  @Mock GetHealthCareUnitMembersService getHealthCareUnitMembersService;

  @Mock CareUnitConverter careUnitConverter;

  @InjectMocks GetCareUnitService getCareUnitService;

  private Commission c1;
  private Unit unit;
  private HealthCareUnitMembers members;
  private Vardenhet convertedUnit;

  @BeforeEach
  void setup() {
    c1 = new Commission();
    convertedUnit = new Vardenhet();
  }

  @Test
  void shouldReturnNullIfUnitNotFoundFromHsa() {
    when(getUnitService.get(any())).thenReturn(null);

    final var response = getCareUnitService.get(c1);

    assertNull(response);
  }

  @Nested
  class UnitAndMembersFound {

    @BeforeEach
    void setup() {
      unit = new Unit();
      unit.setUnitHsaId(ID);

      members = new HealthCareUnitMembers();

      when(getUnitService.get(any())).thenReturn(unit);

      when(careUnitConverter.convert(any(), any(), any())).thenReturn(convertedUnit);

      when(getHealthCareUnitMembersService.get(any())).thenReturn(members);
    }

    @Test
    void shouldSendCommissionToConverter() {
      final var captor = ArgumentCaptor.forClass(Commission.class);

      getCareUnitService.get(c1);

      verify(careUnitConverter)
          .convert(captor.capture(), any(Unit.class), any(HealthCareUnitMembers.class));
      assertEquals(c1, captor.getValue());
    }

    @Test
    void shouldSendUnitFromHsaToConverter() {
      final var captor = ArgumentCaptor.forClass(Unit.class);

      getCareUnitService.get(c1);

      verify(careUnitConverter)
          .convert(any(Commission.class), captor.capture(), any(HealthCareUnitMembers.class));
      assertEquals(unit, captor.getValue());
    }

    @Test
    void shouldSendMembersFromHsaToConverter() {
      final var captor = ArgumentCaptor.forClass(HealthCareUnitMembers.class);

      getCareUnitService.get(c1);

      verify(careUnitConverter).convert(any(Commission.class), any(Unit.class), captor.capture());
      assertEquals(members, captor.getValue());
    }

    @Test
    void shouldReturnConvertedUnit() {
      final var response = getCareUnitService.get(c1);

      assertEquals(convertedUnit, response);
    }
  }

  @Nested
  class GetCareUnitWithUnitRequests {

    private final GetUnitRequestDTO unitRequest = GetUnitRequestDTO.builder().hsaId(ID).build();
    private final GetHealthCareUnitMembersRequestDTO unitMembersRequest =
        GetHealthCareUnitMembersRequestDTO.builder().hsaId(ID).build();

    private Unit unit;
    private HealthCareUnitMembers unitMembers;

    @BeforeEach
    void init() {
      unit = new Unit();
      unitMembers = new HealthCareUnitMembers();
    }

    @Test
    void shouldThrowIllegalArgumentIfHsaIdIsEmptyString() {
      assertThrows(IllegalArgumentException.class, () -> getCareUnitService.get(""));
    }

    @Test
    void shouldReturnVardenhet() {
      final var expected = new Vardenhet(ID, "name");
      when(getUnitService.get(unitRequest)).thenReturn(unit);
      when(getHealthCareUnitMembersService.get(unitMembersRequest)).thenReturn(unitMembers);
      when(careUnitConverter.convert(unit, unitMembers)).thenReturn(expected);

      final var careUnit = getCareUnitService.get(ID);
      assertEquals(ID, careUnit.getId());
    }

    @Test
    void shouldReturnNullWhenUnitIsNull() {
      when(getUnitService.get(unitRequest)).thenReturn(null);

      final var careUnit = getCareUnitService.get(ID);
      verifyNoInteractions(careUnitConverter);
      assertNull(careUnit);
    }
  }
}
