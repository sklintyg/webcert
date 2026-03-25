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
package se.inera.intyg.webcert.infra.sjukfall.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.sjukfall.dto.IntygData;
import se.inera.intyg.webcert.infra.sjukfall.dto.SjukfallIntyg;
import se.inera.intyg.webcert.infra.sjukfall.testdata.SjukfallIntygGenerator;

/**
 * @author Magnus Ekstrand on 2017-08-31.
 */
@ExtendWith(MockitoExtension.class)
class SjukfallIntygPatientCreatorTest {

  private static final String LOCATION_INTYGSDATA =
      "classpath:Sjukfall/Patient/intygsdata-patient.csv";

  private static List<IntygData> intygDataList;

  private SjukfallIntygPatientCreator testee;

  private LocalDate activeDate = LocalDate.parse("2016-02-16");

  @BeforeAll
  static void initTestData() throws IOException {
    SjukfallIntygGenerator generator = new SjukfallIntygGenerator(LOCATION_INTYGSDATA);
    intygDataList = generator.generate().get();

    assertEquals(5, intygDataList.size(), "Expected 5 but was " + intygDataList.size());
  }

  @BeforeEach
  void setup() {
    testee = new SjukfallIntygPatientCreator();
  }

  @Test
  void testCreatingMapWithMaxGlappZeroDays() {
    Map<Integer, List<SjukfallIntyg>> map = testee.createMap(intygDataList, 0, activeDate);
    assertEquals(4, map.size(), "Expected 4 but was " + map.size());
  }

  @Test
  void testCreatingMapWithMaxGlappOneDays() {
    Map<Integer, List<SjukfallIntyg>> map = testee.createMap(intygDataList, 1, activeDate);
    assertEquals(3, map.size(), "Expected 3 but was " + map.size());
  }

  @Test
  void testCreatingMapWithMaxGlappTwoDays() {
    Map<Integer, List<SjukfallIntyg>> map = testee.createMap(intygDataList, 2, activeDate);
    assertEquals(2, map.size(), "Expected 3 but was " + map.size());
  }

  @Test
  void testCreatingMapWithMaxGlappThreeDays() {
    Map<Integer, List<SjukfallIntyg>> map = testee.createMap(intygDataList, 3, activeDate);
    assertEquals(2, map.size(), "Expected 2 but was " + map.size());
  }

  @Test
  void testCreatingMapWithMaxGlappNineDays() {
    Map<Integer, List<SjukfallIntyg>> map = testee.createMap(intygDataList, 9, activeDate);
    assertEquals(2, map.size(), "Expected 2 but was " + map.size());
  }

  @Test
  void testCreatingMapWithMaxGlappTenDays() {
    Map<Integer, List<SjukfallIntyg>> map = testee.createMap(intygDataList, 10, activeDate);
    assertEquals(1, map.size(), "Expected 2 but was " + map.size());
  }

  @Test
  void testCreatingMapWithMaxGlappElevenDays() {
    Map<Integer, List<SjukfallIntyg>> map = testee.createMap(intygDataList, 11, activeDate);
    assertEquals(1, map.size(), "Expected 4 but was " + map.size());
  }
}
