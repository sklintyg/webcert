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
package se.inera.intyg.webcert.infra.sjukfall.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.sjukfall.dto.IntygData;
import se.inera.intyg.webcert.infra.sjukfall.dto.SjukfallIntyg;
import se.inera.intyg.webcert.infra.sjukfall.testdata.SjukfallIntygGenerator;

/** Created by Magnus Ekstrand on 2016-02-16. */
@ExtendWith(MockitoExtension.class)
class SjukfallIntygTest {

  private static final String LOCATION_INTYGSDATA = "classpath:Sjukfall/Enhet/intygsdata.csv";

  private static List<IntygData> intygDataList;

  private final LocalDate activeDate = LocalDate.parse("2016-02-16");

  @BeforeAll
  static void initTestData() throws IOException {
    SjukfallIntygGenerator generator = new SjukfallIntygGenerator(LOCATION_INTYGSDATA);
    intygDataList = generator.generate().get();

    assertEquals(6, intygDataList.size(), "Expected 6 but was " + intygDataList.size());
  }

  @Test
  void testIntyg1() {
    IntygData intygData = getIntygsData("intyg-1");
    SjukfallIntyg testee = new SjukfallIntyg.SjukfallIntygBuilder(intygData, activeDate, 0).build();

    assertIntygsData(testee, "2016-02-01", "2016-02-10", false);
  }

  @Test
  void testIntyg2() {
    IntygData intygData = getIntygsData("intyg-2");
    SjukfallIntyg testee = new SjukfallIntyg.SjukfallIntygBuilder(intygData, activeDate, 0).build();

    assertIntygsData(testee, "2016-02-12", "2016-02-20", true);
  }

  @Test
  void testIntyg3() {
    IntygData intygData = getIntygsData("intyg-3");
    SjukfallIntyg testee = new SjukfallIntyg.SjukfallIntygBuilder(intygData, activeDate, 0).build();

    assertIntygsData(testee, "2016-02-01", "2016-02-20", true);
  }

  @Test
  void testIntyg4() {
    IntygData intygData = getIntygsData("intyg-4");
    SjukfallIntyg testee = new SjukfallIntyg.SjukfallIntygBuilder(intygData, activeDate, 0).build();

    assertIntygsData(testee, "2016-02-01", "2016-02-25", false);
  }

  @Test
  void testIntyg5() {
    IntygData intygData = getIntygsData("intyg-5");
    SjukfallIntyg testee = new SjukfallIntyg.SjukfallIntygBuilder(intygData, activeDate, 0).build();

    assertIntygsData(testee, "2016-02-01", "2016-02-28", true);
  }

  @Test
  void testIntyg6() {
    IntygData intygData = getIntygsData("intyg-6");
    SjukfallIntyg testee1 =
        new SjukfallIntyg.SjukfallIntygBuilder(intygData, activeDate, 0).build();
    SjukfallIntyg testee2 =
        new SjukfallIntyg.SjukfallIntygBuilder(intygData, LocalDate.parse("2016-02-22"), 0).build();
    SjukfallIntyg testee3 =
        new SjukfallIntyg.SjukfallIntygBuilder(intygData, LocalDate.parse("2016-02-23"), 0).build();

    assertIntygsData(testee1, "2016-02-11", "2016-02-28", false);
    assertIntygsData(testee2, "2016-02-11", "2016-02-28", true);
    assertIntygsData(testee3, "2016-02-11", "2016-02-28", true);
  }

  private IntygData getIntygsData(String intygsId) {
    return intygDataList.stream()
        .filter(e -> e.getIntygId().equalsIgnoreCase(intygsId))
        .findAny()
        .orElseThrow();
  }

  private static void assertIntygsData(
      SjukfallIntyg obj, String startDatum, String slutDatum, boolean aktivtIntyg) {
    assertEquals(obj.getStartDatum(), LocalDate.parse(startDatum));
    assertEquals(obj.getSlutDatum(), LocalDate.parse(slutDatum));
    assertEquals(obj.isAktivtIntyg(), aktivtIntyg);
  }
}
