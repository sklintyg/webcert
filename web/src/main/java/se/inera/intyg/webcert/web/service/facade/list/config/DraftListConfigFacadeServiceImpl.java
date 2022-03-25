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
import se.inera.intyg.webcert.web.service.facade.list.DraftStatusDTO;
import se.inera.intyg.webcert.web.service.facade.list.ForwardedTypeDTO;

import java.util.*;

@Service
public class DraftListConfigFacadeServiceImpl implements ListConfigFacadeService {

    @Override
    public ListConfigDTO get() {
        return getListDraftsConfig();
    }

    private ListConfigDTO getListDraftsConfig() {
        final var config = new ListConfigDTO();
        config.setTitle("Ej signerade utkast");
        config.setPageSizes(List.of(10, 20, 50, 100));
        config.setFilters(getListDraftsFilters());
        config.setOpenCertificateTooltip("Öppna utkastet.");
        config.setTableHeadings(getTableHeadings());
        config.setDefaultOrderBy(ListColumnTypeDTO.SAVED);
        return config;
    }

    public TableHeadingDTO[] getTableHeadings() {
        return new TableHeadingDTO[] {
                new TableHeadingDTO(ListColumnTypeDTO.CERTIFICATE_TYPE_NAME,"Typ av intyg"),
                new TableHeadingDTO(ListColumnTypeDTO.STATUS, "Status"),
                new TableHeadingDTO(ListColumnTypeDTO.SAVED, "Senast sparat"),
                new TableHeadingDTO(ListColumnTypeDTO.PATIENT_ID,"Patient"),
                new TableHeadingDTO(ListColumnTypeDTO.SAVED_BY, "Sparat av"),
                new TableHeadingDTO(ListColumnTypeDTO.FORWARDED, "Vidarebefordrad"),
                new TableHeadingDTO(ListColumnTypeDTO.CERTIFICATE_ID, "")
        };
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
                    ListFilterValueDTO.create(ForwardedTypeDTO.SHOW_ALL.toString(), ForwardedTypeDTO.SHOW_ALL.getName(), true),
                    ListFilterValueDTO.create(ForwardedTypeDTO.FORWARDED.toString(), ForwardedTypeDTO.FORWARDED.getName(), false),
                    ListFilterValueDTO.create(ForwardedTypeDTO.NOT_FORWARDED.toString(),ForwardedTypeDTO.NOT_FORWARDED.getName(), false)
                )
        );
    }

    private ListFilterConfigDTO getDraftStatusFilter() {
        return new ListFilterSelectConfigDTO("STATUS", "Utkast", List.of(
                        ListFilterValueDTO.create(DraftStatusDTO.SHOW_ALL.toString(), DraftStatusDTO.SHOW_ALL.getName(), true),
                        ListFilterValueDTO.create(DraftStatusDTO.INCOMPLETE.toString(), DraftStatusDTO.INCOMPLETE.getName(), false),
                        ListFilterValueDTO.create(DraftStatusDTO.COMPLETE.toString(), DraftStatusDTO.COMPLETE.getName(), false),
                        ListFilterValueDTO.create(DraftStatusDTO.LOCKED.toString(), DraftStatusDTO.LOCKED.getName(), false)
                )
        );
    }

    private ListFilterConfigDTO getPatientIdFilter() {
        return new ListFilterPersonIdConfigDTO("PATIENT_ID", "Patient", "åååå-mm-dd");
    }

    private ListFilterConfigDTO getSavedDateRangeFilter() {
        final var to = new ListFilterDateConfigDTO("TO", "Till");
        final var from = new ListFilterDateConfigDTO("FROM", "Från");
        return new ListFilterDateRangeConfigDTO("SAVED", "Sparat datum", to, from);
    }

    // add doctor name
}
