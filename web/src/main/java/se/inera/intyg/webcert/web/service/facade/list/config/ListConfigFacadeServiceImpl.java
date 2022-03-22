/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.list.config;

import org.springframework.stereotype.Service;
import java.util.*;

import static se.inera.intyg.webcert.web.service.facade.list.config.ListTypeDTO.DRAFTS;

@Service
public class ListConfigFacadeServiceImpl implements ListConfigFacadeService {

    @Override
    public ListConfigDTO get(ListTypeDTO type) {
        if (type == DRAFTS) {
            return getListDraftsConfig();
        }
        return new ListConfigDTO();
    }

    private ListConfigDTO getListDraftsConfig() {
        final var config = new ListConfigDTO();
        config.setTitle("Ej signerade utkast");
        config.setPageSizes(List.of(10, 20, 50, 100));
        config.setFilters(getListDraftsFilters());
        return config;
    }

    private List<ListFilterConfigDTO> getListDraftsFilters() {
        final var filters = new ArrayList<ListFilterConfigDTO>();
        filters.add(getForwardedFilter());
        filters.add(getDraftStatusFilter());
        filters.add(getPatientIdFilter());
        filters.add(getSavedDateRangeFilter());
        return filters;
    }

    private ListFilterConfigDTO getForwardedFilter() {
        return new ListFilterSelectConfigDTO("FORWARDED", "Vidarebefordrat",
                List.of(
                    ListFilterValueDTO.create("SHOW_ALL", "Visa alla", true),
                    ListFilterValueDTO.create("FORWARDED", "Vidarebefordrade", false),
                    ListFilterValueDTO.create("NOT_FORWARDED", "Ej vidarebefordrade", false)
                )
        );
    }

    private ListFilterConfigDTO getDraftStatusFilter() {
        return new ListFilterSelectConfigDTO("STATUS", "Utkast", List.of(
                        ListFilterValueDTO.create("SHOW_ALL", "Visa alla", true),
                        ListFilterValueDTO.create("INCOMPLETE", "Uppgifter saknas", false),
                        ListFilterValueDTO.create("COMPLETE", "Kan signeras", false),
                        ListFilterValueDTO.create("LOCKED", "Låsta", false)
                )
        );
    }

    private ListFilterConfigDTO getPatientIdFilter() {
        return new ListFilterTextConfigDTO("PATIENT_ID", "Patient", "åååå-mm-dd");
    }

    private ListFilterConfigDTO getSavedDateRangeFilter() {
        final var to = new ListFilterDateConfigDTO("TO", "Till");
        final var from = new ListFilterDateConfigDTO("FROM", "Från");
        return new ListFilterDateRangeConfigDTO("SAVED", "Sparat datum", to, from);
    }
}
