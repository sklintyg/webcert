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
package se.inera.intyg.webcert.web.service.facade.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;


public record CareUnitContext(List<String> allUnitIds, List<String> careUnitIds, Set<String> selectedUnitIds,
                              Map<String, List<String>> careUnitToSubUnits, Map<String, String> careUnitToCareProviderId,
                              boolean maxCommissionsExceeded) {

    public static CareUnitContext build(WebCertUser user, int maxCommissionsForStatistics) {
        final var careProviders = user.getVardgivare();
        if (careProviders == null) {
            return null;
        }

        final var careUnitData = extractCareUnitData(careProviders);
        if (careUnitData.careUnitIds().isEmpty()) {
            return null;
        }

        final boolean exceeded = careUnitData.careUnitIds().size() > maxCommissionsForStatistics;
        final var allUnitIds = determineAllUnitIds(user, exceeded);
        final var selectedUnitIds = extractSelectedUnitIds(user);

        return new CareUnitContext(
            allUnitIds,
            careUnitData.careUnitIds(),
            selectedUnitIds,
            careUnitData.careUnitToSubUnits(),
            careUnitData.careUnitToCareProviderId(),
            exceeded
        );
    }

    private static CareUnitData extractCareUnitData(List<Vardgivare> careProviders) {
        final List<String> careUnitIds = new ArrayList<>();
        final Map<String, List<String>> careUnitToSubUnits = new HashMap<>();
        final Map<String, String> careUnitToCareProviderId = new HashMap<>();

        for (Vardgivare careProvider : careProviders) {
            for (Vardenhet careUnit : careProvider.getVardenheter()) {
                final String careUnitId = careUnit.getId();
                if (careUnitId == null) {
                    continue;
                }
                careUnitIds.add(careUnitId);
                careUnitToCareProviderId.put(careUnitId, careProvider.getId());
                careUnitToSubUnits.put(careUnitId, extractSubUnitIds(careUnit, careUnitId));
            }
        }

        return new CareUnitData(careUnitIds, careUnitToSubUnits, careUnitToCareProviderId);
    }

    private static List<String> extractSubUnitIds(Vardenhet careUnit, String careUnitId) {
        return careUnit.getHsaIds().stream()
            .filter(id -> id != null && !id.equals(careUnitId))
            .toList();
    }

    private static List<String> determineAllUnitIds(WebCertUser user, boolean exceeded) {
        if (exceeded && user.getValdVardenhet() != null) {
            return new ArrayList<>(user.getValdVardenhet().getHsaIds());
        }
        if (!exceeded && user.getIdsOfAllVardenheter() != null) {
            return new ArrayList<>(user.getIdsOfAllVardenheter());
        }
        return List.of();
    }

    private static Set<String> extractSelectedUnitIds(WebCertUser user) {
        return user.getIdsOfSelectedVardenhet() != null
            ? new HashSet<>(user.getIdsOfSelectedVardenhet())
            : Set.of();
    }

    public List<String> getSubUnitsFor(String careUnitId) {
        return careUnitToSubUnits.getOrDefault(careUnitId, List.of());
    }

    public String getCareProviderIdFor(String careUnitId) {
        return careUnitToCareProviderId.get(careUnitId);
    }

    public List<String> getNotSelectedUnitIds() {
        return allUnitIds.stream()
            .filter(id -> !selectedUnitIds.contains(id))
            .toList();
    }
}