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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Commission;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardenhet;

@ExtendWith(MockitoExtension.class)
class GetCareUnitListServiceTest {

  private static final Vardenhet EXPECTED_UNIT = new Vardenhet();

  @Mock private GetCareUnitService getCareUnitService;

  @InjectMocks private GetCareUnitListService getCareUnitListService;

  private Commission active;
  private Commission inactive;

  @BeforeEach
  void setup() {
    active = new Commission();
    inactive = new Commission();

    EXPECTED_UNIT.setId("ID");
    EXPECTED_UNIT.setNamn("NAMN");

    active.setHealthCareUnitStartDate(LocalDateTime.now().minusDays(1));
    active.setHealthCareUnitEndDate(LocalDateTime.now().plusDays(5));

    inactive.setHealthCareUnitStartDate(LocalDateTime.now().plusDays(1));
    inactive.setHealthCareUnitEndDate(LocalDateTime.now().plusDays(5));
  }

  @Test
  void shouldFilterInactiveCommissions() {
    when(getCareUnitService.get(any(Commission.class))).thenReturn(EXPECTED_UNIT);
    final var response = getCareUnitListService.get(List.of(active, inactive));

    assertEquals(1, response.size());
    assertEquals(EXPECTED_UNIT, response.get(0));
  }

  @Test
  void shouldFilterNullUnits() {
    when(getCareUnitService.get(any(Commission.class))).thenReturn(null);
    final var response = getCareUnitListService.get(List.of(active, inactive));

    assertTrue(response.isEmpty());
  }

  @Test
  void shouldFilterDuplicatedCommissions() {
    when(getCareUnitService.get(any(Commission.class))).thenReturn(EXPECTED_UNIT);
    final var response = getCareUnitListService.get(List.of(active, active));

    assertEquals(1, response.size());
    assertEquals(EXPECTED_UNIT, response.get(0));
  }

  @Test
  void shouldSendActiveCommissionToConverter() {
    when(getCareUnitService.get(any(Commission.class))).thenReturn(EXPECTED_UNIT);
    final var captor = ArgumentCaptor.forClass(Commission.class);

    getCareUnitListService.get(List.of(active, inactive));

    verify(getCareUnitService).get(captor.capture());
    assertEquals(active, captor.getValue());
  }
}
