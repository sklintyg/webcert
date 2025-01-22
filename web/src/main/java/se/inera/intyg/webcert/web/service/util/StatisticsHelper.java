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
package se.inera.intyg.webcert.web.service.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticsHelper {

    public static Map<String, Long> mergeArendeAndFragaSvarMaps(Map<String, Long> fragaSvarStatsMap, Map<String, Long> arendeStatsMap) {
        Map<String, Long> mergedMap = new HashMap<>();

        Set<String> uniqueEnhetsId = Stream.of(fragaSvarStatsMap.keySet(), arendeStatsMap.keySet()).flatMap(Collection::stream).distinct()
            .collect(Collectors.toSet());

        for (String enhetId : uniqueEnhetsId) {
            Long sum = (fragaSvarStatsMap.get(enhetId) != null ? fragaSvarStatsMap.get(enhetId) : 0)
                + (arendeStatsMap.get(enhetId) != null ? arendeStatsMap.get(enhetId) : 0);
            mergedMap.put(enhetId, sum);
        }
        return mergedMap;
    }
}
