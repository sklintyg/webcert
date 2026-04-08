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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.webcert.infra.sjukfall.dto.IntygData;
import se.inera.intyg.webcert.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.webcert.infra.sjukfall.dto.SjukfallIntyg;

/**
 * @author Magnus Ekstrand on 2017-02-10.
 */
public class SjukfallIntygEnhetCreator {

  private static final Logger LOG = LoggerFactory.getLogger(SjukfallIntygEnhetCreator.class);

  // - - - API - - -

  public Map<String, List<SjukfallIntyg>> create(
      List<IntygData> intygData, IntygParametrar parameters) {
    LOG.debug("Start creating a map of 'sjukfallintyg'...");

    Map<String, List<SjukfallIntyg>> map;

    map = createMap(intygData, parameters);
    map = reduceMap(map);
    map = sortValues(map);
    map = setActive(map);

    LOG.debug("...stop creating a map of 'sjukfallintyg'.");
    return map;
  }

  // - - - Package scope - - -

  Map<String, List<SjukfallIntyg>> createMap(
      List<IntygData> intygsData, IntygParametrar parameters) {
    LOG.debug("  1. Create the map");

    Map<String, List<SjukfallIntyg>> map = new HashMap<>();

    for (IntygData i : intygsData) {
      String k = i.getPatientId();

      map.computeIfAbsent(k, k1 -> new ArrayList<>());
      SjukfallIntyg v =
          new SjukfallIntyg.SjukfallIntygBuilder(
                  i, parameters.getAktivtDatum(), parameters.getMaxAntalDagarSedanSjukfallAvslut())
              .build();
      map.get(k).add(v);
    }

    return map;
  }

  Map<String, List<SjukfallIntyg>> reduceMap(Map<String, List<SjukfallIntyg>> map) {
    LOG.debug("  2. Reduce map - filter out each entry where there is no active certificate.");

    return map.entrySet().stream()
        .filter(
            e ->
                e.getValue().stream()
                        .filter(o -> o.isAktivtIntyg() || o.isNyligenAvslutat())
                        .count()
                    > 0)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Method returns a map with sorted values. The sorting is done on SjukfallIntyg objects'
   * slutDatum. Objects are arranged in ascending order, i.e object with biggest slutDatum will be
   * last.
   *
   * @param unsortedMap a map with patients current certificates
   * @return a map with patients current certificates sorted in ascending order
   */
  Map<String, List<SjukfallIntyg>> sortValues(Map<String, List<SjukfallIntyg>> unsortedMap) {
    LOG.debug("  3. Sort map - sort each entry by its end date using ascending order.");

    // Lambda comparator
    Comparator<SjukfallIntyg> dateComparator =
        (o1, o2) -> o1.getSlutDatum().compareTo(o2.getSlutDatum());

    return unsortedMap.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().stream().sorted(dateComparator).collect(Collectors.toList())));
  }

  Map<String, List<SjukfallIntyg>> setActive(Map<String, List<SjukfallIntyg>> map) {
    LOG.debug(
        "  4. Set the active certificate - there can be only one active certificate, find it and make it active.");

    return map.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e -> e.setValue(setActive(e.getValue())).stream().collect(Collectors.toList())));
  }

  private List<SjukfallIntyg> setActive(List<SjukfallIntyg> intygsDataList) {
    List<SjukfallIntyg> values = new ArrayList<>();

    int aktivtIntygIndex = 0;
    SjukfallIntyg sjukfallIntyg = null;

    ListIterator<SjukfallIntyg> iterator = intygsDataList.listIterator();
    while (iterator.hasNext()) {
      int currentIndex = iterator.nextIndex();
      SjukfallIntyg current = iterator.next();

      // Add current object to new list
      values.add(current);

      if (current.isAktivtIntyg()) {

        if (sjukfallIntyg == null) {
          sjukfallIntyg = current;
          aktivtIntygIndex = currentIndex;
          continue;
        }

        LocalDateTime dtAktivt = sjukfallIntyg.getSigneringsTidpunkt();
        LocalDateTime dtCurrent = current.getSigneringsTidpunkt();

        if (dtAktivt.isBefore(dtCurrent)) {
          // Change active status
          sjukfallIntyg.setAktivtIntyg(false);
          // Update new list
          values.set(aktivtIntygIndex, sjukfallIntyg);
          // Swap
          sjukfallIntyg = current;
          aktivtIntygIndex = currentIndex;
        } else {
          // Change active status
          current.setAktivtIntyg(false);
          // Update new list
          values.set(currentIndex, current);
        }
      }
    }

    return values;
  }
}
