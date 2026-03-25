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
package se.inera.intyg.webcert.infra.postnummer.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.postnummer.model.Omrade;

@ExtendWith(MockitoExtension.class)
class PostnummerRepositoryFactoryTest {

  private PostnummerRepositoryFactory factory = new PostnummerRepositoryFactory();

  private static final String LINE_1 = "13100;NACKA;01;STOCKHOLM;0182;NACKA";
  private static final String LINE_1_POSTNUMMER = "13100";
  private static final String LINE_1_POSTORT = "NACKA";
  private static final String LINE_1_LAN = "STOCKHOLM";
  private static final String LINE_1_KOMMUN = "NACKA";

  @Test
  void testCreateOmradeFromString() {

    Omrade res = factory.createOmradeFromString(LINE_1);

    assertNotNull(res);
    assertEquals(LINE_1_POSTNUMMER, res.getPostnummer());
    assertEquals(LINE_1_POSTORT, res.getPostort());
    assertEquals(LINE_1_KOMMUN, res.getKommun());
    assertEquals(LINE_1_LAN, res.getLan());
  }

  @Test
  void testCreateOmradeWithSetters() {
    Omrade control = factory.createOmradeFromString(LINE_1);
    Omrade test = new Omrade(null, null, null, null);
    test.setKommun(LINE_1_KOMMUN);
    test.setLan(LINE_1_LAN);
    test.setPostnummer(LINE_1_POSTNUMMER);
    test.setPostort(LINE_1_POSTORT);
    assertTrue(control.hashCode() == test.hashCode());
  }
}
