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
package se.inera.intyg.webcert.infra.sjukfall.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * @author Magnus Ekstrand on 2017-02-20.
 */
class DiagnosKodTest {

  @Test
  void testCleanDignosKod() {
    assertEquals("M80-", DiagnosKod.cleanKod("M80-   "));
    assertEquals("M80-P", DiagnosKod.cleanKod("M80-P  "));
    assertEquals("M80", DiagnosKod.cleanKod("M80.   "));
    assertEquals("M801", DiagnosKod.cleanKod("M80.1   "));
    assertEquals("M8001", DiagnosKod.cleanKod("M.80.01   "));
    assertEquals("A", DiagnosKod.cleanKod("aÅÄÖ)(/"));
  }

  @Test
  void testSplitNormalDignosKod() {
    DiagnosKod kod = DiagnosKod.create("M123   Palindrom reumatism");

    assertEquals("M123", kod.getCleanedCode());
    assertEquals("Palindrom reumatism", kod.getName());
  }

  @Test
  void testSplitToShortDiagnosKod() {
    DiagnosKod kod = DiagnosKod.create("M123");

    assertEquals("M123", kod.getCleanedCode());
    assertNull(kod.getName());
  }

  @Test
  void testEmptyDiagnosKod() {
    assertThrows(IllegalArgumentException.class, () -> DiagnosKod.create(""));
  }

  @Test
  void testNullDiagnosKod() {
    assertThrows(IllegalArgumentException.class, () -> DiagnosKod.create(null));
  }
}
