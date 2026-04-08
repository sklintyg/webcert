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
package se.inera.intyg.webcert.infra.sjukfall.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.sjukfall.dto.Formaga;
import se.inera.intyg.webcert.infra.sjukfall.dto.IntygData;
import se.inera.intyg.webcert.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.webcert.infra.sjukfall.dto.Lakare;
import se.inera.intyg.webcert.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.webcert.infra.sjukfall.dto.SjukfallIntyg;
import se.inera.intyg.webcert.infra.sjukfall.dto.SjukfallPatient;
import se.inera.intyg.webcert.infra.sjukfall.dto.Vardenhet;
import se.inera.intyg.webcert.infra.sjukfall.dto.Vardgivare;
import se.inera.intyg.webcert.infra.sjukfall.engine.SjukfallIntygEnhetCreator;
import se.inera.intyg.webcert.infra.sjukfall.engine.SjukfallIntygEnhetResolver;
import se.inera.intyg.webcert.infra.sjukfall.testdata.SjukfallIntygGenerator;

/** Created by martin on 11/02/16. */
@ExtendWith(MockitoExtension.class)
class SjukfallEngineServiceTest {

  private static final String LOCATION_INTYGSDATA =
      "classpath:SjukfallServiceTest/intygsdata-engine.csv";

  private static List<IntygData> intygDataList;
  private static List<SjukfallEnhet> sjukfallListUnit;
  private static List<SjukfallPatient> sjukfallListPatient;

  private LocalDate activeDate = LocalDate.parse("2016-02-16");

  @Spy SjukfallIntygEnhetCreator creator = new SjukfallIntygEnhetCreator();

  @Spy private SjukfallIntygEnhetResolver resolver = new SjukfallIntygEnhetResolver(creator);

  @InjectMocks private SjukfallEngineServiceImplTest testee = new SjukfallEngineServiceImplTest();

  @BeforeEach
  void init() throws IOException {
    // Load test data
    SjukfallIntygGenerator generator = new SjukfallIntygGenerator(LOCATION_INTYGSDATA);
    intygDataList = generator.generate().get();
    assertInit(intygDataList, 33);

    sjukfallListUnit =
        testee.beraknaSjukfallForEnhet(intygDataList, getIntygParametrar(5, activeDate));
    assertInit(sjukfallListUnit, 11);

    List<IntygData> intygDataListPatient =
        intygDataList.stream().filter(o -> o.getPatientId().equals("19710301-1032")).toList();

    sjukfallListPatient =
        testee.beraknaSjukfallForPatient(intygDataListPatient, getIntygParametrar(5, activeDate));
    assertInit(sjukfallListPatient, 2);
  }

  // ~
  // ======================================================================================================== ~
  // ~ Specification för test av sjukfall #1 - #12 finns på URL:
  // ~ https://inera-certificate.atlassian.net/wiki/pages/viewpage.action?pageId=39747618
  // ~
  // ======================================================================================================== ~

  @Test
  void testCalculateSjukfallEnhet1() {
    assertSjukfallEnhet("19791110-9291", "2016-02-01", "2016-02-20", 2, 19);
  }

  @Test
  void testCalculateSjukfallEnhet2() {
    assertSjukfallEnhet("19791123-9262", "2016-02-01", "2016-02-20", 2, 19);
  }

  @Test
  void testCalculateSjukfallEnhet3() {
    assertSjukfallEnhet("19791212-9280", "2016-02-01", "2016-02-25", 3, 24);
  }

  @Test
  void testCalculateSjukfallEnhet4() {
    assertSjukfallEnhet("19800113-9297", "2016-02-01", "2016-02-25", 3, 24);
  }

  @Test
  void testCalculateSjukfallEnhet5() {
    assertSjukfallEnhet("19800124-9286", "2016-02-12", "2016-02-25", 2, 14);
  }

  @Test
  void testCalculateSjukfallEnhet6() {
    assertSjukfallEnhet("19800207-9294", "2016-02-12", "2016-02-25", 2, 14);
  }

  @Test
  void testCalculateSjukfallEnhet7() {
    assertSjukfallEnhet("19800228-9224", "2016-02-01", "2016-02-25", 0, 0);
  }

  @Test
  void testCalculateSjukfallEnhet8() {
    assertSjukfallEnhet("19961110-2394", "2016-02-01", "2016-02-19", 3, 15);
  }

  @Test
  void testCalculateSjukfallEnhet9() {
    assertSjukfallEnhet("19961111-2385", "2016-02-15", "2016-03-04", 3, 15);
  }

  @Test
  void testCalculateSjukfallEnhet10() {
    assertSjukfallEnhet("19571109-2642", "2016-02-15", "2016-02-19", 1, 5);
  }

  @Test
  void testCalculateSjukfallEnhet11() {
    assertSjukfallEnhet(
        "19630206-2846",
        "2016-02-01",
        "2016-03-04",
        4,
        29,
        Arrays.asList("fall-11-intyg-1", "fall-11-intyg-2", "fall-11-intyg-3", "fall-11-intyg-4"));
  }

  @Test
  void testCalculateSjukfallEnhet12() {
    assertSjukfallEnhet(
        "19710301-1032",
        "2016-02-15",
        "2016-03-04",
        3,
        19,
        Arrays.asList("fall-12-intyg-2", "fall-12-intyg-3", "fall-12-intyg-4"));
  }

  @Test
  void testCalculateSjukfallPatient() {

    // Säkerställ att listan är rätt sorterad
    List<SjukfallPatient> subList = sjukfallListPatient.subList(1, sjukfallListPatient.size());
    assertSortOrder(sjukfallListPatient.get(0), subList);

    assertSjukfallPatient(sjukfallListPatient.get(0), "2016-02-15", "2016-03-04", 3, 19);
    assertSjukfallPatient(sjukfallListPatient.get(1), "2016-02-01", "2016-02-05", 1, 5);
  }

  @Test
  void shouldContainSysselsattning() {
    assertNotNull(sjukfallListUnit.get(0).getSysselsattning());
  }

  // - - -  Private scope  - - -

  private static void assertInit(List<?> list, int expectedListSize) {
    assertEquals(
        list.size(), expectedListSize, "Expected " + expectedListSize + " but was " + list.size());
  }

  private static void assertSjukfallEnhet(
      String patientId,
      String startDatum,
      String slutDatum,
      int antalIntyg,
      int effektivSjukskrivningslangd) {
    assertSjukfallEnhet(
        patientId, startDatum, slutDatum, antalIntyg, effektivSjukskrivningslangd, null);
  }

  private static void assertSjukfallEnhet(
      String patientId,
      String startDatum,
      String slutDatum,
      int antalIntyg,
      int effektivSjukskrivningslangd,
      List<String> expectedIntygsIds) {
    SjukfallEnhet sjukfallEnhet =
        sjukfallListUnit.stream()
            .filter(o -> o.getPatient().getId().equals(patientId))
            .findFirst()
            .orElse(null);

    if (antalIntyg == 0) {
      assertNull(sjukfallEnhet);
      return;
    }

    assertTrue(sjukfallEnhet.getStart().isEqual(LocalDate.parse(startDatum)));
    assertTrue(sjukfallEnhet.getSlut().isEqual(LocalDate.parse(slutDatum)));
    assertEquals(sjukfallEnhet.getIntyg(), antalIntyg);
    assertEquals(sjukfallEnhet.getDagar(), effektivSjukskrivningslangd);
    if (expectedIntygsIds != null) {
      assertEquals(expectedIntygsIds.size(), sjukfallEnhet.getIntygLista().size());
      assertTrue(sjukfallEnhet.getIntygLista().containsAll(expectedIntygsIds));
    }
  }

  private static void assertSjukfallPatient(
      SjukfallPatient sjukfallPatient,
      String startDatum,
      String slutDatum,
      int antalIntyg,
      int effektivSjukskrivningslangd) {

    assertTrue(sjukfallPatient.getStart().isEqual(LocalDate.parse(startDatum)));
    assertTrue(sjukfallPatient.getSlut().isEqual(LocalDate.parse(slutDatum)));
    assertEquals((int) sjukfallPatient.getDagar(), effektivSjukskrivningslangd);
    assertEquals(sjukfallPatient.getSjukfallIntygList().size(), antalIntyg);

    // Kolla av att intygen är i fallande ordning gällande signeringstidpunkt
    SjukfallIntyg intyg = sjukfallPatient.getSjukfallIntygList().get(0);
    List<SjukfallIntyg> subList =
        sjukfallPatient
            .getSjukfallIntygList()
            .subList(1, sjukfallPatient.getSjukfallIntygList().size());

    assertSortOrder(intyg, subList);
  }

  private static void assertSortOrder(SjukfallPatient obj, List<SjukfallPatient> list) {
    if (obj == null) {
      fail();
    }
    if (list == null || list.isEmpty()) {
      return;
    }

    assertTrue(obj.getStart().isAfter(list.get(0).getStart()));
    assertSortOrder(list.get(0), list.subList(1, list.size()));
  }

  private static void assertSortOrder(SjukfallIntyg obj, List<SjukfallIntyg> list) {
    if (obj == null) {
      fail();
    }
    if (list == null || list.isEmpty()) {
      return;
    }

    // assert the capacity for work sort order
    assertGrader(obj.getIntygId(), obj.getGrader());

    assertTrue(obj.getStartDatum().isAfter(list.get(0).getStartDatum()));
    assertSortOrder(list.get(0), list.subList(1, list.size()));
  }

  private static void assertGrader(String intygId, List<Integer> grader) {
    IntygData data =
        intygDataList.stream()
            .filter(obj -> obj.getIntygId().equalsIgnoreCase(intygId))
            .findFirst()
            .get();
    List<Formaga> formagor =
        data.getFormagor().stream().sorted(Comparator.comparing(Formaga::getStartdatum)).toList();

    assertEquals(grader.size(), formagor.size());

    for (int i = 0; i < grader.size(); i++) {
      assertEquals(grader.get(i).intValue(), formagor.get(i).getNedsattning());
    }
  }

  private IntygParametrar getIntygParametrar(int maxIntygsGlapp, LocalDate aktivtDatum) {
    return new IntygParametrar(maxIntygsGlapp, aktivtDatum);
  }

  private class SjukfallEngineServiceImplTest extends SjukfallEngineServiceImpl {

    public SjukfallEngineServiceImplTest() {
      super();
      // 2016-02-11
      final int date = 1455203622;
      clock = Clock.fixed(Instant.ofEpochSecond(date), ZoneId.systemDefault());
    }

    @Override
    protected SjukfallEnhet buildSjukfallEnhet(
        List<SjukfallIntyg> values,
        SjukfallIntyg aktivtIntyg,
        LocalDate aktivtDatum,
        IntygParametrar intygParameters) {
      Vardgivare vardgivare = Vardgivare.create(" IFV1239877878-0000 ", "Webcert-Vårdgivare1");
      Vardenhet vardenhet = Vardenhet.create(" IFV1239877878-1045 ", "Webcert-Enhet2");
      Lakare lakare = Lakare.create(aktivtIntyg.getLakareId(), aktivtIntyg.getLakareNamn());

      SjukfallEnhet sjukfallEnhet =
          super.buildSjukfallEnhet(values, aktivtIntyg, aktivtDatum, intygParameters);
      sjukfallEnhet.setVardgivare(vardgivare);
      sjukfallEnhet.setVardenhet(vardenhet);
      sjukfallEnhet.setLakare(lakare);
      return sjukfallEnhet;
    }
  }
}
