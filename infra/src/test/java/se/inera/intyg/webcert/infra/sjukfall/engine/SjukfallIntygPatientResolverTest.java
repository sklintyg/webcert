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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.sjukfall.dto.IntygData;
import se.inera.intyg.webcert.infra.sjukfall.dto.SjukfallIntyg;
import se.inera.intyg.webcert.infra.sjukfall.testdata.SjukfallIntygGenerator;

/**
 * @author Magnus Ekstrand on 2017-08-31.
 */
@ExtendWith(MockitoExtension.class)
class SjukfallIntygPatientResolverTest {

  private static final String LOCATION_INTYGSDATA =
      "classpath:Sjukfall/Patient/intygsdata-patient.csv";

  private static List<IntygData> intygDataList;

  private LocalDate activeDate = LocalDate.parse("2016-02-16");

  @Spy private SjukfallIntygPatientCreator creatorSpy;

  private SjukfallIntygPatientResolver testee;

  @BeforeAll
  static void initTestData() throws IOException {
    SjukfallIntygGenerator generator = new SjukfallIntygGenerator(LOCATION_INTYGSDATA);
    intygDataList = generator.generate().get();

    assertEquals(5, intygDataList.size(), "Expected 5 but was " + intygDataList.size());
  }

  @BeforeEach
  void setup() {
    testee = new SjukfallIntygPatientResolver(creatorSpy);
  }

  @Test
  void testHappyDays() {
    Map<Integer, List<SjukfallIntyg>> mockedInstance = Mockito.mock(Map.class);
    Mockito.doReturn(mockedInstance).when(creatorSpy).create(intygDataList, 0, activeDate);

    // invoke testing method
    Map<Integer, List<SjukfallIntyg>> actualInstance = testee.resolve(intygDataList, 0, activeDate);

    assertEquals(actualInstance, mockedInstance);
    Mockito.verify(creatorSpy, Mockito.times(1)).create(intygDataList, 0, activeDate);
  }

  @Test
  void testInvalidArgumentIntygsData() {
    Map<Integer, List<SjukfallIntyg>> map = testee.resolve(new ArrayList<>(), 0, activeDate);
    assertEquals(0, map.size(), "Expected 0 but was " + map.size());
  }

  @Test
  void testInvalidArgumentIntygsGlapp() {
    Map<Integer, List<SjukfallIntyg>> map = testee.resolve(intygDataList, -1, activeDate);
    assertEquals(0, map.size(), "Expected 0 but was " + map.size());
  }
}
