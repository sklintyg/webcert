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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.TableHeading;
import se.inera.intyg.webcert.web.service.facade.list.config.factory.ListFilterConfigFactory;
import se.inera.intyg.webcert.web.service.facade.list.config.factory.TableHeadingFactory;

import java.util.*;

@Service
public class DraftListConfigFacadeServiceImpl implements ListConfigFacadeService {

    private final GetStaffInfoFacadeService getStaffInfoFacadeService;
    private final String TITLE = "Ej signerade utkast";
    private final String OPEN_CERTIFICATE_TOOLTIP = "Öppna utkastet.";
    private final String SEARCH_CERTIFICATE_TOOLTIP = "Sök efter utkast.";

    @Autowired
    public DraftListConfigFacadeServiceImpl(GetStaffInfoFacadeService getStaffInfoFacadeService) {
        this.getStaffInfoFacadeService = getStaffInfoFacadeService;
    }

    @Override
    public ListConfig get() {
        return getListDraftsConfig();
    }

    private ListConfig getListDraftsConfig() {
        final var config = new ListConfig();
        config.setTitle(TITLE);
        config.setFilters(getListDraftsFilters());
        config.setOpenCertificateTooltip(OPEN_CERTIFICATE_TOOLTIP);
        config.setSearchCertificateTooltip(SEARCH_CERTIFICATE_TOOLTIP);
        config.setTableHeadings(getTableHeadings());
        return config;
    }

    public TableHeading[] getTableHeadings() {
        return new TableHeading[] {
                TableHeadingFactory.text(ListColumnType.CERTIFICATE_TYPE_NAME),
                TableHeadingFactory.text(ListColumnType.STATUS),
                TableHeadingFactory.date(ListColumnType.SAVED),
                TableHeadingFactory.patientInfo(ListColumnType.PATIENT_ID),
                TableHeadingFactory.forwarded(ListColumnType.FORWARDED),
                TableHeadingFactory.openButton(ListColumnType.CERTIFICATE_ID)
        };
    }

    private List<ListFilterConfig> getListDraftsFilters() {
        final var filters = new ArrayList<ListFilterConfig>();
        filters.add(ListFilterConfigFactory.forwardedSelect());
        filters.add(ListFilterConfigFactory.draftStatusSelect());
        filters.add(getSavedByFilter());
        filters.add(ListFilterConfigFactory.defaultPersonId());
        filters.add(ListFilterConfigFactory.defaultDateRange());
        filters.add(ListFilterConfigFactory.orderBy(ListColumnType.SAVED));
        filters.add(ListFilterConfigFactory.ascending());
        filters.add(ListFilterConfigFactory.pageSize());
        return filters;
    }

    private ListFilterConfig getSavedByFilter() {
        final var savedByList = getStaffInfoFacadeService.get();
        return ListFilterConfigFactory.createStaffSelect("SAVED_BY", "Sparat av", savedByList, getStaffInfoFacadeService.getLoggedInStaffHsaId());
    }
}
