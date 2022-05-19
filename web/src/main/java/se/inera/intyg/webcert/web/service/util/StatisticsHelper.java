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
