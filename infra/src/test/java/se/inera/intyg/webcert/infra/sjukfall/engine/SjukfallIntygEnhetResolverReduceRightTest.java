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
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.SjukfallIntyg;
import se.inera.intyg.infra.sjukfall.testdata.SjukfallIntygGenerator;

/** Created by Magnus Ekstrand on 10/02/16. */
@ExtendWith(MockitoExtension.class)
class SjukfallIntygEnhetResolverReduceRightTest {

  private static final String LOCATION_INTYGSDATA =
      "classpath:Sjukfall/Enhet/intygsdata-resolver-right.csv";

  private static List<IntygData> intygDataList;

  private SjukfallIntygEnhetResolver resolver;

  @BeforeAll
  static void initTestData() throws IOException {
    SjukfallIntygGenerator generator = new SjukfallIntygGenerator(LOCATION_INTYGSDATA);
    intygDataList = generator.generate().get();
  }

  @BeforeEach
  void setup() {
    resolver = new SjukfallIntygEnhetResolver(new SjukfallIntygEnhetCreator());
  }

  @Test
  void testFall1() {
    List<SjukfallIntyg> result = getTestData("fall-1-right", "2016-02-10", 5, "2016-01-31");
    assertEquals(3, result.size(), "Expected 3 but was " + result.size());
  }

  @Test
  void testFall2() {
    List<SjukfallIntyg> result = getTestData("fall-2-right", "2016-02-10", 5, "2016-01-31");
    assertEquals(2, result.size(), "Expected 2 but was " + result.size());
    assertEquals("fall-2-intyg-1", result.get(0).getIntygId());
    assertEquals("fall-2-intyg-2", result.get(1).getIntygId());
  }

  @Test
  void testFall3() {
    List<SjukfallIntyg> result = getTestData("fall-3-right", "2016-02-10", 5, "2016-01-31");
    assertEquals(0, result.size(), "Expected 0 but was " + result.size());
  }

  @Test
  void testFall4() {
    List<SjukfallIntyg> result = getTestData("fall-4-right", "2016-02-10", 5, "2016-01-31");
    assertEquals(3, result.size(), "Expected 3 but was " + result.size());
    assertEquals("fall-4-intyg-1", result.get(0).getIntygId());
    assertEquals("fall-4-intyg-2", result.get(1).getIntygId());
    assertEquals("fall-4-intyg-3", result.get(2).getIntygId());
  }

  @Test
  void testFall5() {
    List<SjukfallIntyg> result = getTestData("fall-5-right", "2016-02-10", 5, "2016-01-31");
    assertEquals(3, result.size(), "Expected 3 but was " + result.size());
    assertEquals("fall-5-intyg-4", result.get(0).getIntygId());
    assertEquals("fall-5-intyg-1", result.get(1).getIntygId());
    assertEquals("fall-5-intyg-2", result.get(2).getIntygId());
  }

  @Test
  void testFall6() {
    List<SjukfallIntyg> result = getTestData("fall-6-right", "2016-02-10", 5, "2016-01-31");
    assertEquals(2, result.size(), "Expected 2 but was " + result.size());
  }

  private List<SjukfallIntyg> getTestData(
      String key, String aktivtDatum, int maxIntygsGlapp, String initialtDatum) {
    Map<String, List<SjukfallIntyg>> data = getTestData(aktivtDatum);
    return resolver.reduceRight(data.get(key), maxIntygsGlapp, LocalDate.parse(initialtDatum));
  }

  private Map<String, List<SjukfallIntyg>> getTestData(String aktivtDatum) {
    return resolver.createMap(intygDataList, new IntygParametrar(5, LocalDate.parse(aktivtDatum)));
  }
}
