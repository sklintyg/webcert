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
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;
import se.inera.intyg.webcert.web.service.facade.list.config.factory.ListFilterConfigFactory;
import se.inera.intyg.webcert.web.service.facade.list.config.factory.TableHeadingFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

import java.util.ArrayList;
import java.util.List;

@Service
public class ListQuestionsConfigFacadeServiceImpl implements ListConfigFacadeService {

    private static final String TITLE = "Ej hanterade ärenden";
    private static final String OPEN_CERTIFICATE_TOOLTIP = "Öppnar intyget och frågan/svaret.";
    private static final String SEARCH_CERTIFICATE_TOOLTIP = "Sök efter frågor och svar.";
    private static final String FORWARD_CERTIFICATE_TOOLTIP = "Skapar ett e-postmeddelande i din e-postklient med en direktlänk till frågan/svaret.";
    private static final String DESCRIPTION = "Nedan visas alla ej hanterade ärenden, så som kompletteringsbegäran och administrativa frågor, för den eller de enheter du väljer.";
    private static final String EMPTY_LIST_TEXT = "Det finns inga ohanterade ärenden för den enhet eller de enheter du är inloggad på.";
    private static final String RESET_FILTER_TOOLTIP = "Återställ sökfilter för ej hanterade ärenden.";

    private final GetStaffInfoFacadeService getStaffInfoFacadeService;
    private final WebCertUserService webCertUserService;

    @Autowired
    public ListQuestionsConfigFacadeServiceImpl(GetStaffInfoFacadeService getStaffInfoFacadeService,
                                                WebCertUserService webCertUserService) {
        this.getStaffInfoFacadeService = getStaffInfoFacadeService;
        this.webCertUserService = webCertUserService;
    }

    @Override
    public ListConfig get() {
        return getListDraftsConfig();
    }

    private ListConfig getListDraftsConfig() {
        final var config = new ListConfig();
        config.setTitle(TITLE);
        config.setFilters(getFilters());
        config.addButtonTooltip(CertificateListItemValueType.OPEN_BUTTON.toString(), OPEN_CERTIFICATE_TOOLTIP);
        config.addButtonTooltip(CertificateListItemValueType.SEARCH_BUTTON.toString(), SEARCH_CERTIFICATE_TOOLTIP);
        config.addButtonTooltip(CertificateListItemValueType.RESET_BUTTON.toString(), RESET_FILTER_TOOLTIP);
        config.addButtonTooltip(CertificateListItemValueType.FORWARD_BUTTON.toString(), FORWARD_CERTIFICATE_TOOLTIP);
        config.setTableHeadings(getTableHeadings());
        config.setDescription(DESCRIPTION);
        config.setEmptyListText(EMPTY_LIST_TEXT);
        config.setSecondaryTitle(getSecondaryTitle());
        return config;
    }

    private String getSecondaryTitle() {
        final var user = webCertUserService.getUser();
        return "Ärenden visas för valda enheter";
    }

    public TableHeading[] getTableHeadings() {
        return new TableHeading[] {
                TableHeadingFactory.text(ListColumnType.QUESTION_ACTION),
                TableHeadingFactory.text(ListColumnType.RECIPIENT),
                TableHeadingFactory.patientInfo(ListColumnType.PATIENT_ID),
                TableHeadingFactory.text(ListColumnType.SIGNED),
                TableHeadingFactory.date(ListColumnType.SENT_RECEIVED, false),
                TableHeadingFactory.forwarded(ListColumnType.FORWARDED, "Visar om ärendet är vidarebefordrat."),
                TableHeadingFactory.forwardButton(ListColumnType.FORWARD_CERTIFICATE),
                TableHeadingFactory.openButton(ListColumnType.OPEN_CERTIFICATE)
        };
    }

    private List<ListFilterConfig> getFilters() {
        final var filters = new ArrayList<ListFilterConfig>();
        //filters.add(ListFilterConfigFactory.unitSelect());
        filters.add(ListFilterConfigFactory.forwardedSelect());
        //filters.add(ListFilterConfigFactory.reasonSelect());
        //filters.add(ListFilterConfigFactory.recipientSelect());
        filters.add(getSignedByFilter());
        filters.add(ListFilterConfigFactory.defaultPersonId());
        //filters.add(ListFilterConfigFactory.sentDateRange());
        filters.add(ListFilterConfigFactory.orderBy(ListColumnType.SENT_RECEIVED));
        filters.add(ListFilterConfigFactory.ascending());
        filters.add(ListFilterConfigFactory.pageSize());
        return filters;
    }

    private ListFilterConfig getSignedByFilter() {
        final var savedByList = getStaffInfoFacadeService.get();
        final var defaultValue = getStaffInfoFacadeService.getLoggedInStaffHsaId();
        return ListFilterConfigFactory.createStaffSelect("SIGNED_BY", "Signerat av", savedByList, defaultValue);
    }
}
