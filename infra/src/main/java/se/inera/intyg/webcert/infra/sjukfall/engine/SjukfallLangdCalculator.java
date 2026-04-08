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

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import se.inera.intyg.webcert.infra.sjukfall.dto.Formaga;
import se.inera.intyg.webcert.infra.sjukfall.dto.IntygData;
import se.inera.intyg.webcert.infra.sjukfall.dto.SjukfallIntyg;

/**
 * Calulates the effective length of all arbetsformaga intervals contained in the
 * SortableIntygsData's, making sure to merge overlapping intervals first.
 *
 * <p>Created by marced on 19/02/16.
 */
public final class SjukfallLangdCalculator {

  private static final int GAP_BETWEEN_INTYG = 5;

  private SjukfallLangdCalculator() {}

  public static int getEffectiveNumberOfSickDaysByIntyg(List<SjukfallIntyg> intygsUnderlag) {
    return getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, null, GAP_BETWEEN_INTYG);
  }

  public static int getEffectiveNumberOfSickDaysByIntyg(
      List<SjukfallIntyg> intygsUnderlag, int gapBetweenIntyg) {
    return getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, null, gapBetweenIntyg);
  }

  public static int getEffectiveNumberOfSickDaysByIntyg(
      List<SjukfallIntyg> intygsUnderlag, LocalDate aktivtDatum) {
    return getEffectiveNumberOfSickDaysByIntyg(intygsUnderlag, aktivtDatum, GAP_BETWEEN_INTYG);
  }

  public static int getEffectiveNumberOfSickDaysByIntyg(
      List<SjukfallIntyg> intygsUnderlag, LocalDate aktivtDatum, int gapBetweenIntyg) {
    if (intygsUnderlag == null || intygsUnderlag.isEmpty()) {
      return 0;
    }

    final SjukfallIntyg activeSickLeaveCertificate =
        getActiveSickLeaveCertificate(intygsUnderlag, aktivtDatum);
    if (aktivtDatum != null && activeSickLeaveCertificate == null) {
      return 0;
    }

    final List<SjukfallIntyg> sickLeaveCertificateListToCalculate =
        getSickLeaveCertificatesToCalculate(
            intygsUnderlag, activeSickLeaveCertificate, gapBetweenIntyg);

    final List<LocalDateInterval> formagorIntervalList =
        sickLeaveCertificateListToCalculate.stream()
            .map(SjukfallLangdCalculator::getIntervalsOfFormagor)
            .collect(Collectors.toList())
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

    final List<LocalDateInterval> mergedFormagorIntervals = mergeIntervals(formagorIntervalList);

    return calculateSumOfIntervals(mergedFormagorIntervals);
  }

  /**
   * This method filters out any {@link SjukfallIntyg} that shouldn't be part of the calculation and
   * returns only a list of those that should. The filter makes sure that any sick leave certificate
   * is either directly or indirectly linked to the active sick leave.
   *
   * @param sickLeaveCertificateList List of {@link SjukfallIntyg} to filter.
   * @param activeSickLeaveCertificate The active {@link SjukfallIntyg}.
   * @param gapBetweenIntyg Allowed gap between intyg in days.
   * @return List of {@link SjukfallIntyg} to use in sick leave length calculation.
   */
  private static List<SjukfallIntyg> getSickLeaveCertificatesToCalculate(
      List<SjukfallIntyg> sickLeaveCertificateList,
      SjukfallIntyg activeSickLeaveCertificate,
      int gapBetweenIntyg) {
    final ArrayList<SjukfallIntyg> sickLeaveCertificatesToConsider = new ArrayList<>();

    sickLeaveCertificateList.sort(Comparator.comparing(SjukfallIntyg::getStartDatum));

    SjukfallIntyg previousSickLeaveCertificate = null;
    boolean consideredActiveSickLeaveCertificate = false;
    for (SjukfallIntyg sjukfallIntyg : sickLeaveCertificateList) {
      if (isSickLeaveCertificateLinkedToPrevious(
          sjukfallIntyg, previousSickLeaveCertificate, gapBetweenIntyg)) {
        sickLeaveCertificatesToConsider.add(sjukfallIntyg);
      } else if (consideredActiveSickLeaveCertificate) {
        // The gap in days between certificates are too big and shouldn't be considered to be part
        // of the same sjukfall.
        // Because we already considered the active sick leave certificate, any certificates with
        // later start date are
        // ignored.
        return sickLeaveCertificatesToConsider;
      } else {
        // The gap in days between certificates are too big and shouldn't be considered to be part
        // of the same sjukfall
        // As we still haven't considered the active sick leave certificate yet, we have to clear
        // the already collected
        // sickleaves and start over.
        sickLeaveCertificatesToConsider.clear();
        sickLeaveCertificatesToConsider.add(sjukfallIntyg);
      }

      // Sick leave certificates can overlap in periods, so we have to consider if the current has a
      // later end date than the previous.
      if (previousSickLeaveCertificate == null
          || sjukfallIntyg.getSlutDatum().isAfter(previousSickLeaveCertificate.getSlutDatum())) {
        previousSickLeaveCertificate = sjukfallIntyg;
      }

      // Keep track if we have considered the active sick leave certificate.
      if (!consideredActiveSickLeaveCertificate) {
        consideredActiveSickLeaveCertificate = sjukfallIntyg.equals(activeSickLeaveCertificate);
      }
    }

    return sickLeaveCertificatesToConsider;
  }

  private static boolean isSickLeaveCertificateLinkedToPrevious(
      SjukfallIntyg sickLeaveCertificate,
      SjukfallIntyg previousSickLeaveCertificate,
      int gapBetweenIntyg) {
    return isFirstSickLeaveCertificate(previousSickLeaveCertificate)
        || !previousSickLeaveCertificate
            .getSlutDatum()
            .plusDays(gapBetweenIntyg + 1)
            .isBefore(sickLeaveCertificate.getStartDatum());
  }

  private static boolean isFirstSickLeaveCertificate(SjukfallIntyg sickLeaveCertificate) {
    return sickLeaveCertificate == null;
  }

  /**
   * Get active {@link SjukfallIntyg} based on activeDate. If multiple {@link SjukfallIntyg} are
   * active at the same time, then the one with the latest sign date is returned. If no {@link
   * SjukfallIntyg} is considered active, then null is returned.
   *
   * @param sickLeaveCertificateList List of {@link SjukfallIntyg} to get the active from.
   * @param activeDate Date to use when considering which intyg is active.
   * @return Active {@link SjukfallIntyg} or null if none is considered active.
   */
  private static SjukfallIntyg getActiveSickLeaveCertificate(
      List<SjukfallIntyg> sickLeaveCertificateList, LocalDate activeDate) {
    final List<SjukfallIntyg> activeSickLeaveCertificateInDecendingSignOrder =
        sickLeaveCertificateList.stream()
            .filter(sjukfallIntyg -> isActive(sjukfallIntyg, activeDate))
            .sorted(
                Comparator.comparing(IntygData::getSigneringsTidpunkt, Comparator.reverseOrder()))
            .collect(Collectors.toList());
    return activeSickLeaveCertificateInDecendingSignOrder.isEmpty()
        ? null
        : activeSickLeaveCertificateInDecendingSignOrder.get(0);
  }

  private static int calculateSumOfIntervals(List<LocalDateInterval> intervals) {
    return (int) intervals.stream().mapToLong(LocalDateInterval::getDurationInDays).sum();
  }

  private static boolean isActive(SjukfallIntyg sickLeaveCertificate, LocalDate activeDate) {
    return activeDate != null
        && !(activeDate.isAfter(sickLeaveCertificate.getSlutDatum())
            || activeDate.isBefore(sickLeaveCertificate.getStartDatum()));
  }

  private static List<LocalDateInterval> getIntervalsOfFormagor(SjukfallIntyg sjukfallIntyg) {
    final List<LocalDateInterval> allIntervals =
        sjukfallIntyg.getFormagor().stream()
            .map(formaga -> new LocalDateInterval(formaga.getStartdatum(), formaga.getSlutdatum()))
            .collect(Collectors.toList());
    return mergeIntervals(allIntervals);
  }

  public static int getEffectiveNumberOfSickDaysByFormaga(List<Formaga> formagaList) {
    return getEffectiveNumberOfSickDaysByFormaga(formagaList, null);
  }

  public static int getEffectiveNumberOfSickDaysByFormaga(
      List<Formaga> formagaList, LocalDate aktivtDatum) {
    // Sanity check
    if (formagaList == null || formagaList.isEmpty()) {
      return 0;
    }

    // extract all grader-intervals from list of Formaga to a list of LocalDateIntervals
    List<LocalDateInterval> allIntervals =
        formagaList.stream()
            .map(formaga -> new LocalDateInterval(formaga.getStartdatum(), formaga.getSlutdatum()))
            .collect(Collectors.toList());

    // apply merge algorithm
    List<LocalDateInterval> mergedIntervals = mergeIntervals(allIntervals);

    // calculate sum of total length of remaining intervals
    return (int) mergedIntervals.stream().mapToLong(LocalDateInterval::getDurationInDays).sum();
  }

  static List<LocalDateInterval> mergeIntervals(List<LocalDateInterval> allIntervals) {

    if (allIntervals.isEmpty()) {
      return allIntervals;
    }
    // 1. Sort them from lowest starting time to highest starting time
    allIntervals.sort(Comparator.comparing(LocalDateInterval::getStartDate));
    Deque<LocalDateInterval> stack = new ArrayDeque<>();

    stack.push(allIntervals.get(0));
    for (int i = 1; i < allIntervals.size(); i++) {
      // get interval from stack top
      LocalDateInterval top = stack.peek();

      // if current interval is not overlapping with stack top, push it to the stack
      LocalDateInterval current = allIntervals.get(i);
      if (top.getEndDate().isBefore(current.getStartDate())) {
        stack.push(current);

        // Otherwise update the ending time of top if ending of current
        // interval is more
      } else if (top.getEndDate().isBefore(current.getEndDate())) {
        top.setEndDate(current.getEndDate());
        stack.pop();
        stack.push(top);
      }
    }

    return stack.stream().collect(Collectors.toList());
  }
}
