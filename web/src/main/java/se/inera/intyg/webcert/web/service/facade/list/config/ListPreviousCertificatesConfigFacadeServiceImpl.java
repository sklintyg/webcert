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

import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;
import se.inera.intyg.webcert.web.service.facade.list.config.factory.ListFilterConfigFactory;
import se.inera.intyg.webcert.web.service.facade.list.config.factory.TableHeadingFactory;

import java.util.ArrayList;
import java.util.List;

@Service
public class ListPreviousCertificatesConfigFacadeServiceImpl implements ListConfigFacadeService {

    private static final String TITLE = "Tidigare intyg";
    private static final String OPEN_CERTIFICATE_TOOLTIP = "Öppnar intyget/utkastet.";
    private static final String RENEW_BUTTON_TOOLTIP =
        "Skapar ett nytt intygsutkast för förlängning av sjukskrivning, där en del information från detta intyg följer med.";
    private static final String EMPTY_LIST_TEXT = "Det finns inga tidigare intyg för patienten.";

    @Override
    public ListConfig get() {
        return getConfig();
    }

    private ListConfig getConfig() {
        final var config = new ListConfig();
        config.setExcludeFilterButtons(true);
        config.setSecondaryTitle(TITLE);
        config.setFilters(getFilters());
        config.addButtonTooltip(CertificateListItemValueType.OPEN_BUTTON.toString(), OPEN_CERTIFICATE_TOOLTIP);
        config.addButtonTooltip(CertificateListItemValueType.RENEW_BUTTON.toString(), RENEW_BUTTON_TOOLTIP);
        config.setTableHeadings(getTableHeadings());
        config.setEmptyListText(EMPTY_LIST_TEXT);
        return config;
    }

    public TableHeading[] getTableHeadings() {
        return new TableHeading[]{
            TableHeadingFactory.text(ListColumnType.CERTIFICATE_TYPE_NAME),
            TableHeadingFactory.text(ListColumnType.STATUS, getStatusDescription()),
            TableHeadingFactory.date(ListColumnType.SAVED),
            TableHeadingFactory.text(ListColumnType.SAVED_SIGNED_BY),
            TableHeadingFactory.renewButton(ListColumnType.RENEW_CERTIFICATE),
            TableHeadingFactory.openButton(ListColumnType.CERTIFICATE_ID)
        };
    }

    private List<ListFilterConfig> getFilters() {
        final var filters = new ArrayList<ListFilterConfig>();
        filters.add(ListFilterConfigFactory.certificateStatusRadio());
        filters.add(ListFilterConfigFactory.orderBy(ListColumnType.SAVED));
        filters.add(ListFilterConfigFactory.ascending());
        filters.add(ListFilterConfigFactory.pageSize());
        return filters;
    }

    private String getStatusDescription() {
        return "Visar utkastets/intygets status:<ul><li>"
            + "Utkast, uppgifter saknas = Utkastet är sparat, men obligatoriska uppgifter saknas.</li>"
            + "<li>Utkast, kan signeras = Utkastet är komplett, sparat och kan signeras.</li>"
            + "<li>Utkast, låst = Utkastet är låst.</li><li>Signerat = Intyget är signerat.</li>"
            + "<li>Skickat = Intyget är skickat till mottagaren.</li>"
            + "<li>Makulerat = Intyget är makulerat.</li><li>Ersatt = Intyget är ersatt.</li>"
            + "<li>Kompletterat = Intyget är kompletterat.</li></ul>";

    }
}
