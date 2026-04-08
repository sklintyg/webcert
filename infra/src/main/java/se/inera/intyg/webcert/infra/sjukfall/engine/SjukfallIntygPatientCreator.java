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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.webcert.infra.sjukfall.dto.IntygData;
import se.inera.intyg.webcert.infra.sjukfall.dto.SjukfallIntyg;

/**
 * @author Magnus Ekstrand on 2017-02-10.
 */
public class SjukfallIntygPatientCreator {

  private static final Logger LOG = LoggerFactory.getLogger(SjukfallIntygPatientCreator.class);

  // - - - API - - -

  public Map<Integer, List<SjukfallIntyg>> create(
      final List<IntygData> intygsData, final int maxIntygsGlapp, final LocalDate aktivtDatum) {
    LOG.debug("Start creating a map of 'sjukfallintyg'...");

    // Create the map
    Map<Integer, List<SjukfallIntyg>> map = createMap(intygsData, maxIntygsGlapp, aktivtDatum);

    LOG.debug("...stop creating a map of 'sjukfallintyg'.");
    return map;
  }

  // - - - Package scope - - -

  Map<Integer, List<SjukfallIntyg>> createMap(
      final List<IntygData> intygsData, final int maxIntygsGlapp, final LocalDate aktivtDatum) {
    LOG.debug("  2. Create the map");

    // Transform intygsdata to an internal format
    List<SjukfallIntyg> sjukfallIntygList = mapIntyg(intygsData, aktivtDatum);

    return collectIntyg(sjukfallIntygList, maxIntygsGlapp);
  }

  // - - - Private scope - - -

  private List<SjukfallIntyg> mapIntyg(List<IntygData> intygsData, LocalDate aktivtDatum) {
    LOG.debug("     a. Transform 'intygsdata' to intermediate format");

    List<SjukfallIntyg> list = new ArrayList<>();
    for (IntygData i : intygsData) {
      SjukfallIntyg v = new SjukfallIntyg.SjukfallIntygBuilder(i, aktivtDatum, 0).build();
      list.add(v);
    }

    return list;
  }

  /** Collect certificates inside a map where each entry's value is a list with SjukfallIntyg. */
  private Map<Integer, List<SjukfallIntyg>> collectIntyg(
      List<SjukfallIntyg> intygsData, int maxIntygsGlapp) {

    Map<Integer, List<SjukfallIntyg>> map = new HashMap<>();

    // Ensure the that indygsData list is sort by 'startDatum' with ascending order
    Comparator<SjukfallIntyg> dateComparator = Comparator.comparing(SjukfallIntyg::getStartDatum);

    List<SjukfallIntyg> sortedList =
        intygsData.stream().sorted(dateComparator).collect(Collectors.toList());

    collectIntyg(sortedList, map, 0, sortedList.get(0), maxIntygsGlapp);

    return map;
  }

  // Iterate over intygsdata and collect continuous certificates.
  private void collectIntyg(
      List<SjukfallIntyg> input,
      Map<Integer, List<SjukfallIntyg>> output,
      Integer key,
      SjukfallIntyg first,
      int maxIntygsGlapp) {

    int tmp = key;

    if (input.contains(first)) {
      input.remove(first);
    }

    // Add to output map
    output.computeIfAbsent(tmp, v -> new ArrayList<>()).add(first);

    // Return if no more input
    if (input.isEmpty()) {
      return;
    }

    // Lookup if the first and second intyg are within the specified amount of days
    // Rule: startDatum - slutDatum < maxIntygsGlapp
    SjukfallIntyg second = input.get(0);
    if (first.getSlutDatum().plusDays(maxIntygsGlapp + 1).isBefore(second.getStartDatum())) {
      tmp++;
    }

    // Recursive call
    collectIntyg(input, output, tmp, second, maxIntygsGlapp);
  }
}
