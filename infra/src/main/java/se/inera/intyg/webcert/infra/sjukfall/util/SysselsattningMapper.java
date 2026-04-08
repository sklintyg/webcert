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
package se.inera.intyg.webcert.infra.sjukfall.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Magnus Ekstrand on 2017-09-07.
 */
public class SysselsattningMapper extends Mapper {

  public static List<String> mapSysselsattning(List<String> values) {
    return values.stream()
        .map(SysselsattningMapper::mapSysselsattning)
        .collect(Collectors.toList());
  }

  public static String mapSysselsattning(String key) {
    return sysselsattningMap().get(key);
  }

  private static Map<String, String> sysselsattningMap() {
    return Collections.unmodifiableMap(
        Stream.of(
                entry("ARBETSLOSHET", "Arbetssökande"),
                entry("ARBETSSOKANDE", "Arbetssökande"),
                entry("FORALDRALEDIG", "Föräldraledighet"),
                entry("FORALDRALEDIGHET", "Föräldraledighet"),
                entry("NUVARANDE_ARBETE", "Nuvarande arbete"),
                entry("STUDIER", "Studier"))
            .collect(entriesToMap()));
  }
}
