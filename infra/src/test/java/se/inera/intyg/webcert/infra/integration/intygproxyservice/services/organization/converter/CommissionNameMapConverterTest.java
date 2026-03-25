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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Commission;

class CommissionNameMapConverterTest {

  private static final Commission C1 = new Commission();
  private static final Commission C3 = new Commission();
  private static final String C1_ID = "ID1";
  private static final String C1_NAME = "NAME1";
  private static final Commission C2 = new Commission();
  private static final String C2_ID = "ID2";
  private static final String C2_NAME = "NAME2";
  private static final String C3_NAME = "NAME3";

  private final CommissionNameMapConverter converter = new CommissionNameMapConverter();

  @BeforeEach
  void setup() {
    C1.setHealthCareUnitHsaId(C1_ID);
    C2.setHealthCareUnitHsaId(C2_ID);
    C3.setHealthCareUnitHsaId(C1_ID);
    C1.setCommissionName(C1_NAME);
    C2.setCommissionName(C2_NAME);
    C3.setCommissionName(C3_NAME);
  }

  @Test
  void shouldReturnEmptyMapForEmptyList() {
    final var response = converter.convert(Collections.emptyList());

    assertTrue(response.isEmpty());
  }

  @Test
  void shouldReturnMapWithIdAndNameForOneElement() {
    final var response = converter.convert(List.of(C1));

    assertEquals(1, response.size());
    assertTrue(response.containsKey(C1_ID));
    assertEquals(C1_NAME, response.get(C1_ID));
  }

  @Test
  void shouldReturnMapWithIdAndNameForSeveralElements() {
    final var response = converter.convert(List.of(C1, C2));

    assertEquals(2, response.size());
    assertTrue(response.containsKey(C1_ID));
    assertTrue(response.containsKey(C2_ID));
    assertEquals(C1_NAME, response.get(C1_ID));
    assertEquals(C2_NAME, response.get(C2_ID));
  }

  @Test
  void shouldHandleMultipleCommissonsWithSameCareUnitHsaId() {
    final var response = converter.convert(List.of(C1, C3));

    assertEquals(1, response.size());
    assertTrue(response.containsKey(C1_ID));
    assertEquals(C3_NAME, response.get(C1_ID));
  }

  @Test
  void shouldHandleDuplicatedObjects() {
    assertDoesNotThrow(() -> converter.convert(List.of(C1, C1)));
  }
}
