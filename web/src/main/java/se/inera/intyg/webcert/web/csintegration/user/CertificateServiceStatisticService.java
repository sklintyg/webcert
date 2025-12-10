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

package se.inera.intyg.webcert.web.csintegration.user;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.hsatk.model.legacy.AbstractVardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.StatisticsForUnitDTO;
import se.inera.intyg.webcert.web.service.facade.user.UnitStatisticsDTO;
import se.inera.intyg.webcert.web.service.facade.user.UserStatisticsDTO;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
@RequiredArgsConstructor
public class CertificateServiceStatisticService {
    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;

    public void add(UserStatisticsDTO statisticsFromWC, List<String> unitIds, WebCertUser user, boolean maxCommissionsExceeded) {

        final var statisticsFromCS = csIntegrationService.getStatistics(
            csIntegrationRequestFactory.getStatisticsRequest(unitIds)
        );

        if (user.getValdVardenhet() != null) {
            statisticsFromWC.addNbrOfDraftsOnSelectedUnit(getNbrOfDraftsOnSelectedUnit(user, statisticsFromCS));
            statisticsFromWC.addNbrOfUnhandledQuestionsOnSelectedUnit(getNbrOfUnhandledQuestionsOnSelectedUnit(user, statisticsFromCS));
            statisticsFromWC.addTotalDraftsAndUnhandledQuestionsOnOtherUnits(
                getTotalDraftsAndUnhandledQuestionsOnOtherUnits(user, statisticsFromCS)
            );
        }

        if (!maxCommissionsExceeded) {
            final var careUnitsWithRelatedSubUnits = getAvailableCareUnitsWithSubUnits(user.getVardgivare());
            final var unitStatisticsDTO = buildUnitStatisticsFromCS(statisticsFromCS, careUnitsWithRelatedSubUnits);
            statisticsFromWC.mergeUnitStatistics(unitStatisticsDTO);
        }
    }

    private static Map<String, UnitStatisticsDTO> buildUnitStatisticsFromCS(Map<String, StatisticsForUnitDTO> statisticsFromCS,
        Map<String, List<String>> careUnitsWithRelatedSubUnits) {
        return statisticsFromCS.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                statisticForUnit -> {
                    final var unitStatistics = new UnitStatisticsDTO();
                    final var statistics = statisticForUnit.getValue();
                    unitStatistics.setDraftsOnUnit(statistics.getDraftCount());
                    unitStatistics.setQuestionsOnUnit(statistics.getUnhandledMessageCount());
                    if (careUnitsWithRelatedSubUnits.containsKey(statisticForUnit.getKey())) {
                        final var subUnits = careUnitsWithRelatedSubUnits.get(statisticForUnit.getKey());
                        subUnits.forEach(unit -> {
                            if (!statisticsFromCS.containsKey(unit)) {
                                return;
                            }
                            unitStatistics.addQuestionsOnSubUnits(statisticsFromCS.get(unit).getUnhandledMessageCount());
                            unitStatistics.addDraftsOnSubUnits(statisticsFromCS.get(unit).getDraftCount());
                        });
                    }
                    return unitStatistics;
                }
            ));
    }

    private static Map<String, List<String>> getAvailableCareUnitsWithSubUnits(List<Vardgivare> careProviders) {
        return careProviders.stream()
            .map(Vardgivare::getVardenheter)
            .flatMap(List::stream)
            .collect(Collectors.toMap(
                AbstractVardenhet::getId,
                unit -> {
                    final var subUnits = unit.getHsaIds();
                    subUnits.remove(unit.getId());
                    return subUnits;
                }
            ));
    }

    private static long getTotalDraftsAndUnhandledQuestionsOnOtherUnits(WebCertUser user,
        Map<String, StatisticsForUnitDTO> statisticsFromCS) {
        return statisticsFromCS.keySet().stream()
            .filter(unitId -> !user.getIdsOfSelectedVardenhet().contains(unitId))
            .mapToLong(unitId -> statisticsFromCS.get(unitId).getDraftCount() + statisticsFromCS.get(unitId).getUnhandledMessageCount())
            .sum();
    }

    private static int getNbrOfUnhandledQuestionsOnSelectedUnit(WebCertUser user, Map<String, StatisticsForUnitDTO> statisticsFromCS) {
        return statisticsFromCS.keySet().stream()
            .filter(unitId -> user.getValdVardenhet().getId().equals(unitId))
            .findFirst()
            .map(unitId -> statisticsFromCS.get(unitId).getUnhandledMessageCount())
            .orElse(0);
    }

    private static int getNbrOfDraftsOnSelectedUnit(WebCertUser user, Map<String, StatisticsForUnitDTO> statisticsFromCS) {
        return statisticsFromCS.keySet().stream()
            .filter(unitId -> user.getValdVardenhet().getId().equals(unitId))
            .findFirst()
            .map(unitId -> statisticsFromCS.get(unitId).getDraftCount())
            .orElse(0);
    }
}
