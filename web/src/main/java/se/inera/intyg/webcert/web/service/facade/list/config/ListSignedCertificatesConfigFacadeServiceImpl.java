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
package se.inera.intyg.webcert.web.service.facade.list.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;
import se.inera.intyg.webcert.web.service.facade.list.config.factory.ListFilterConfigFactory;
import se.inera.intyg.webcert.web.service.facade.list.config.factory.TableHeadingFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

import java.util.ArrayList;
import java.util.List;

@Service
public class ListSignedCertificatesConfigFacadeServiceImpl implements ListConfigFacadeService {

    private static final String TITLE = "Signerade intyg";
    private static final String OPEN_CERTIFICATE_TOOLTIP = "Öppnar intyget.";
    private static final String SEARCH_CERTIFICATE_TOOLTIP = "Sök efter signerade intyg.";
    private static final String RESET_FILTER_TOOLTIP = "Återställ sökfilter för signerade intyg.";
    private static final String DESCRIPTION = "Nedan visas dina signerade intyg för den enhet du är inloggad på.";
    private static final String EMPTY_LIST_TEXT =
        "Det finns inga signerade intyg de senaste 3 månaderna för den enhet du är inloggad på.";

    private final WebCertUserService webCertUserService;

    @Autowired
    public ListSignedCertificatesConfigFacadeServiceImpl(WebCertUserService webCertUserService) {
        this.webCertUserService = webCertUserService;
    }

    @Override
    public ListConfig get() {
        return getListDraftsConfig();
    }

    private ListConfig getListDraftsConfig() {
        final var config = new ListConfig();
        config.setTitle(TITLE);
        config.setFilters(getListDraftsFilters());
        config.addButtonTooltip(CertificateListItemValueType.OPEN_BUTTON.toString(), OPEN_CERTIFICATE_TOOLTIP);
        config.addButtonTooltip(CertificateListItemValueType.SEARCH_BUTTON.toString(), SEARCH_CERTIFICATE_TOOLTIP);
        config.addButtonTooltip(CertificateListItemValueType.RESET_BUTTON.toString(), RESET_FILTER_TOOLTIP);
        config.setTableHeadings(getTableHeadings());
        config.setDescription(DESCRIPTION);
        config.setEmptyListText(EMPTY_LIST_TEXT);
        config.setSecondaryTitle(getSecondaryTitle());
        return config;
    }

    private String getSecondaryTitle() {
        final var user = webCertUserService.getUser();
        return "Intyg visas för " + user.getValdVardenhet().getNamn();
    }

    public TableHeading[] getTableHeadings() {
        return new TableHeading[]{
            TableHeadingFactory.text(ListColumnType.CERTIFICATE_TYPE_NAME),
            TableHeadingFactory.text(ListColumnType.STATUS, getStatusDescription()),
            TableHeadingFactory.date(ListColumnType.SIGNED),
            TableHeadingFactory.patientInfo(ListColumnType.PATIENT_ID),
            TableHeadingFactory.openButton(ListColumnType.CERTIFICATE_ID)
        };
    }

    private List<ListFilterConfig> getListDraftsFilters() {
        final var filters = new ArrayList<ListFilterConfig>();
        filters.add(ListFilterConfigFactory.defaultPersonId());
        filters.add(ListFilterConfigFactory.signedDateRange());
        filters.add(ListFilterConfigFactory.orderBy(ListColumnType.SIGNED));
        filters.add(ListFilterConfigFactory.ascending());
        filters.add(ListFilterConfigFactory.pageSize());
        return filters;
    }

    private String getStatusDescription() {
        return "<p>Visar signerade intygets status:<ul><li>Skickat= intyget är signerat och skickat till mottagaren."
            + "</li><li>Ej skickat= intyget är signerat men inte skickat, intyget kan öppnas och skickas.</li></p>";
    }
}
