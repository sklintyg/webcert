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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.inera.intyg.infra.sjukfall.dto.Formaga;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.SjukfallIntyg;

/** Created by marced on 19/02/16. */
class SjukfallLangdCalculatorTest {

  private static final LocalDate AKTIVT_DATUM = LocalDate.parse("2016-02-16");
  private static final LocalDateTime SIGNERINGSTIDPUNKT =
      LocalDateTime.parse("2016-02-01T12:00:00");

  @Test
  void testGetEffectiveNumberOfSickDaysNointervals() {

    assertEquals(0, SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(null));

    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();
    assertEquals(0, SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag));
  }

  /** Test FALL1 from confluence document /Krav/Rehabstod/Berakning av sjukfall. */
  @Test
  void testGetEffectiveNumberOfSickDaysFall1() {

    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();

    // First add a simple intyg with a simple interval
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-01T12:00:00"),
            createInterval("2016-02-01", "2016-02-10")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-12T12:00:00"),
            createInterval("2016-02-12", "2016-02-20")));
    assertEquals(
        19,
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, AKTIVT_DATUM));
  }

  /** Test FALL2 from confluence document /Krav/Rehabstod/Berakning av sjukfall. */
  @Test
  void testGetEffectiveNumberOfSickDaysFall2() {

    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();

    // First add a simple intyg with a simple interval
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-01T12:00:00"),
            createInterval("2016-02-01", "2016-02-10")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-01-30T12:00:00"),
            createInterval("2016-02-12", "2016-02-20")));
    assertEquals(
        19,
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, AKTIVT_DATUM));
  }

  /** Test FALL3 from confluence document /Krav/Rehabstod/Berakning av sjukfall. */
  @Test
  void testGetEffectiveNumberOfSickDaysFall3() {

    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();

    // First add a simple intyg with a simple interval
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-01T12:00:00"),
            createInterval("2016-02-01", "2016-02-10")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-12T12:00:00"),
            createInterval("2016-02-12", "2016-02-20")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-20T12:00:00"),
            createInterval("2016-02-21", "2016-02-25")));
    assertEquals(
        24,
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, AKTIVT_DATUM));
  }

  /**
   * Test FALL3 from confluence document /Krav/Rehabstod/Berakning av sjukfall. But with 11 days GAP
   * allowed.
   */
  @Test
  void testGetEffectiveNumberOfSickDaysFall3WithElevenDaysGap() {
    final var gapBetweenIntyg = 11;

    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();

    // First add a simple intyg with a simple interval
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-01T12:00:00"),
            createInterval("2016-02-01", "2016-02-10")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-12T12:00:00"),
            createInterval("2016-02-12", "2016-02-20")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-03-01T12:00:00"),
            createInterval("2016-03-01", "2016-03-05")));
    assertEquals(
        24,
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(
            intygsUnderlag, AKTIVT_DATUM, gapBetweenIntyg));
  }

  /** Test FALL4 from confluence document /Krav/Rehabstod/Berakning av sjukfall. */
  @Test
  void testGetEffectiveNumberOfSickDaysFall4() {

    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();

    // First add a simple intyg with a simple interval
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-01T12:00:00"),
            createInterval("2016-02-01", "2016-02-10")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-12T12:00:00"),
            createInterval("2016-02-12", "2016-02-20")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-11T12:00:00"),
            createInterval("2016-02-21", "2016-02-25")));
    assertEquals(
        24,
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, AKTIVT_DATUM));
  }

  /** Test FALL5 from confluence document /Krav/Rehabstod/Berakning av sjukfall. */
  @Test
  void testGetEffectiveNumberOfSickDaysFall5() {

    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();

    // First add a simple intyg with a simple interval
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-12T12:00:00"),
            createInterval("2016-02-12", "2016-02-20")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-11T12:00:00"),
            createInterval("2016-02-12", "2016-02-25")));
    assertEquals(
        14,
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, AKTIVT_DATUM));
  }

  /** Test FALL6 from confluence document /Krav/Rehabstod/Berakning av sjukfall. */
  @Test
  void testGetEffectiveNumberOfSickDaysFall6() {

    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();

    // First add a simple intyg with a simple interval
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-12T12:00:00"),
            createInterval("2016-02-12", "2016-02-20")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-15T12:00:00"),
            createInterval("2016-02-15", "2016-02-25")));
    assertEquals(
        14,
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, AKTIVT_DATUM));
  }

  /** Test FALL6 from confluence document /Krav/Rehabstod/Berakning av sjukfall. */
  @Test
  void testGetEffectiveNumberOfSickDaysFall7() {

    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();

    // First add a simple intyg with a simple interval
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-01T12:00:00"),
            createInterval("2016-02-01", "2016-02-14")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-15T12:00:00").plusDays(10),
            createInterval("2016-02-18", "2016-02-25")));
    assertEquals(
        0,
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, AKTIVT_DATUM));
  }

  /** Test FALL8 from confluence document /Krav/Rehabstod/Berakning av sjukfall. */
  @Test
  void testGetEffectiveNumberOfSickDaysFall8() {

    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();

    // First add a simple intyg with a simple interval
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-01T12:00:00"),
            createInterval("2016-02-01", "2016-02-05")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-07T12:00:00"),
            createInterval("2016-02-08", "2016-02-12")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-14T12:00:00"),
            createInterval("2016-02-15", "2016-02-19")));
    assertEquals(
        15,
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, AKTIVT_DATUM));
  }

  /** Test FALL9 from confluence document /Krav/Rehabstod/Berakning av sjukfall. */
  @Test
  void testGetEffectiveNumberOfSickDaysFall9() {

    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();

    // First add a simple intyg with a simple interval
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-14T12:00:00"),
            createInterval("2016-02-15", "2016-02-19")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-20T12:00:00"),
            createInterval("2016-02-22", "2016-02-26")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-29T12:00:00"),
            createInterval("2016-02-29", "2016-03-04")));
    assertEquals(
        15,
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, AKTIVT_DATUM));
  }

  /** Test FALL10 from confluence document /Krav/Rehabstod/Berakning av sjukfall. */
  @Test
  void testGetEffectiveNumberOfSickDaysFall10() {

    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();

    // First add a simple intyg with a simple interval
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-01-31T12:00:00"),
            createInterval("2016-02-01", "2016-02-05")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-14T12:00:00"),
            createInterval("2016-02-15", "2016-02-19")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-29T12:00:00"),
            createInterval("2016-02-29", "2016-03-04")));
    assertEquals(
        5,
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, AKTIVT_DATUM));
  }

  /** Test FALL11 from confluence document /Krav/Rehabstod/Berakning av sjukfall. */
  @Test
  void testGetEffectiveNumberOfSickDaysFall11() {

    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();

    // First add a simple intyg with a simple interval
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-01-31T12:00:00"),
            createInterval("2016-02-01", "2016-02-05")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-07T12:00:00"),
            createInterval("2016-02-08", "2016-02-26")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-14T12:00:00"),
            createInterval("2016-02-15", "2016-02-19")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-29T12:00:00"),
            createInterval("2016-02-29", "2016-03-04")));
    assertEquals(
        29,
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, AKTIVT_DATUM));
  }

  /** Test FALL12 from confluence document /Krav/Rehabstod/Berakning av sjukfall. */
  @Test
  void testGetEffectiveNumberOfSickDaysFall12() {

    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();

    // First add a simple intyg with a simple interval
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-01-31T12:00:00"),
            createInterval("2016-02-01", "2016-02-05")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-14T12:00:00"),
            createInterval("2016-02-15", "2016-02-19")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-17T12:00:00"),
            createInterval("2016-02-17", "2016-02-28")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-29T12:00:00"),
            createInterval("2016-02-29", "2016-03-04")));
    assertEquals(
        19,
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, AKTIVT_DATUM));
  }

  /** Test that intervals that are abut are counted equals as spanning the entire period. */
  @Test
  void testGetEffectiveNumberOfSickDaysAbutIntervals() {

    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();
    intygsUnderlag.add(
        createIntyg(
            SIGNERINGSTIDPUNKT,
            createInterval("2016-02-12", "2016-02-20"),
            createInterval("2016-02-21", "2016-02-26"),
            createInterval("2016-02-26", "2016-03-19")));

    List<SjukfallIntyg> intygsUnderlag2 = new ArrayList<>();
    intygsUnderlag2.add(
        createIntyg(SIGNERINGSTIDPUNKT, createInterval("2016-02-12", "2016-03-19")));

    assertEquals(37, SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag));
    assertEquals(37, SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag2));
  }

  @Test
  void testGetEffectiveNumberOfSickDays() {
    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();

    // First add a simple intyg with a simple interval
    intygsUnderlag.add(createIntyg(SIGNERINGSTIDPUNKT, createInterval("2016-01-20", "2016-02-10")));
    assertEquals(22, SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag));

    // Add one that should be "swallowed" entirely by the first and should therefore not effect the
    // length
    intygsUnderlag.add(createIntyg(SIGNERINGSTIDPUNKT, createInterval("2016-02-02", "2016-02-10")));
    assertEquals(22, SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag));

    // Add another intyg where one of the intervals should overlap/extend the first (by 2 days) and
    // adds  separate
    // interval of 20 days
    intygsUnderlag.add(
        createIntyg(
            SIGNERINGSTIDPUNKT,
            createInterval("2016-01-25", "2016-02-12"),
            createInterval("2016-02-20", "2016-03-10")));
    assertEquals(
        22 + 2 + 20, SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag));

    // add another that effectively encompasses all the previous ones except a few days of the last
    // one
    intygsUnderlag.add(createIntyg(SIGNERINGSTIDPUNKT, createInterval("2016-01-02", "2016-03-08")));
    assertEquals(69, SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag));
  }

  @Test
  void testMergeIntervalsSimple() {

    List<LocalDateInterval> intervals = new ArrayList<>();
    final LocalDateInterval a = createInterval("2016-01-01", "2016-01-20");
    final LocalDateInterval b = createInterval("2016-01-20", "2016-02-10");
    intervals.add(a);
    intervals.add(b);

    final List<LocalDateInterval> result = SjukfallLangdCalculator.mergeIntervals(intervals);

    assertEquals(1, result.size());
    assertEquals(a.getStartDate(), result.get(0).getStartDate());
    assertEquals(b.getEndDate(), result.get(0).getEndDate());
  }

  @Test
  void testMergeIntervalsDontMergeDiscreteIntervals() {

    List<LocalDateInterval> intervals = new ArrayList<>();
    final LocalDateInterval a = createInterval("2016-01-01", "2016-01-20");
    final LocalDateInterval b = createInterval("2016-01-21", "2016-02-10");
    intervals.add(a);
    intervals.add(b);

    final List<LocalDateInterval> result = SjukfallLangdCalculator.mergeIntervals(intervals);

    assertEquals(2, result.size());
    assertTrue(result.contains(a));
    assertTrue(result.contains(b));
  }

  @Test
  void testMergeIntervalsComplex() {

    // First add a simple intyg with a simple interval
    LocalDateInterval a = createInterval("2016-01-20", "2016-02-10");

    // Add one that should be "swallowed" entirely by the first and should therefore not effect the
    // length
    LocalDateInterval aCopy = createInterval("2016-02-02", "2016-02-10");

    // Add another intyg where one of the intervals should overlap/extend the first (by 2 days) and
    // adds  separate
    // interval of 20 days
    final LocalDateInterval b1 = createInterval("2016-01-25", "2016-02-12");
    final LocalDateInterval b2 = createInterval("2016-02-20", "2016-03-10");

    // add another that effectively encompasses all the previous ones except a few days of the last
    // one
    final LocalDateInterval c = createInterval("2016-01-02", "2016-03-08");

    List<LocalDateInterval> intervals = new ArrayList<>();
    intervals.add(a);
    intervals.add(aCopy);
    intervals.add(b1);
    intervals.add(b2);
    intervals.add(c);

    final List<LocalDateInterval> result = SjukfallLangdCalculator.mergeIntervals(intervals);
    assertEquals(1, result.size());
    assertEquals(c.getStartDate(), result.get(0).getStartDate());
    assertEquals(b2.getEndDate(), result.get(0).getEndDate());
  }

  @Test
  void testProperReturnValuesWhenZeroDayGapBetweenSjukfall() {
    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-01T12:00:00"),
            createInterval("2016-02-01", "2016-02-10")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-11T12:00:00"),
            createInterval("2016-02-11", "2016-02-20")));

    final var value1 =
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, 0);
    final var value2 =
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, 1);
    final var value3 =
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, 2);

    assertEquals(20, value1);
    assertEquals(20, value2);
    assertEquals(20, value3);
  }

  @Test
  void testProperReturnValuesWhenOneDayGapBetweenSjukfall() {
    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-01T12:00:00"),
            createInterval("2016-02-01", "2016-02-10")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-12T12:00:00"),
            createInterval("2016-02-12", "2016-02-20")));

    final var value1 =
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, 0);
    final var value2 =
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, 1);
    final var value3 =
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, 2);

    assertEquals(9, value1);
    assertEquals(19, value2);
    assertEquals(19, value3);
  }

  @Test
  void testProperReturnValuesWhenFiveDayGapBetweenSjukfall() {
    List<SjukfallIntyg> intygsUnderlag = new ArrayList<>();
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-01T12:00:00"),
            createInterval("2016-02-01", "2016-02-10")));
    intygsUnderlag.add(
        createIntyg(
            LocalDateTime.parse("2016-02-16T12:00:00"),
            createInterval("2016-02-16", "2016-02-20")));

    final var value1 =
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, 4);
    final var value2 =
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, 5);
    final var value3 =
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, 6);

    assertEquals(5, value1);
    assertEquals(15, value2);
    assertEquals(15, value3);
  }

  private SjukfallIntyg createIntyg(
      LocalDateTime signeringsTidpunkt, LocalDateInterval... intervals) {
    final List<Formaga> formagor = new ArrayList<>();

    for (LocalDateInterval i : intervals) {
      int nedsattning = 100;
      formagor.add(new Formaga(i.getStartDate(), i.getEndDate(), nedsattning));
    }

    IntygData intygData = new IntygData();
    intygData.setFormagor(formagor);
    intygData.setSigneringsTidpunkt(signeringsTidpunkt);

    SjukfallIntyg.SjukfallIntygBuilder builder =
        new SjukfallIntyg.SjukfallIntygBuilder(intygData, LocalDate.now(), 0);
    return builder.build();
  }

  private LocalDateInterval createInterval(String startDate, String endDate) {
    return new LocalDateInterval(LocalDate.parse(startDate), LocalDate.parse(endDate));
  }
}
