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
import se.inera.intyg.infra.integration.hsatk.model.legacy.AbstractVardenhet;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;
import se.inera.intyg.webcert.web.service.facade.list.config.factory.ListFilterConfigFactory;
import se.inera.intyg.webcert.web.service.facade.list.config.factory.TableHeadingFactory;
import se.inera.intyg.webcert.web.service.facade.user.UnitStatisticsDTO;
import se.inera.intyg.webcert.web.service.facade.user.UserStatisticsDTO;
import se.inera.intyg.webcert.web.service.facade.user.UserStatisticsService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ListQuestionsConfigFacadeServiceImpl implements ListVariableConfigFacadeService {

    private static final String TITLE = "Ej hanterade ärenden";
    private static final String OPEN_CERTIFICATE_TOOLTIP = "Öppnar intyget och frågan/svaret.";
    private static final String SEARCH_CERTIFICATE_TOOLTIP = "Sök efter frågor och svar.";
    private static final String DESCRIPTION =
            "Nedan visas ej hanterade ärenden, kompletteringsbegäran och administrativa frågor, för den eller de enheter du väljer.";
    private static final String EMPTY_LIST_TEXT =
            "Det finns inga ohanterade ärenden för den enhet eller de enheter du är inloggad på.";
    private static final String RESET_FILTER_TOOLTIP = "Återställ sökfilter för ej hanterade ärenden.";

    private final GetStaffInfoFacadeService getStaffInfoFacadeService;
    private final UserStatisticsService userStatisticsService;
    private final WebCertUserService webCertUserService;
    private final HsaOrganizationsService hsaOrganizationsService;

    @Autowired
    public ListQuestionsConfigFacadeServiceImpl(GetStaffInfoFacadeService getStaffInfoFacadeService,
                                                UserStatisticsService userStatisticsService,
                                                WebCertUserService webCertUserService,
                                                HsaOrganizationsService hsaOrganizationsService) {
        this.getStaffInfoFacadeService = getStaffInfoFacadeService;
        this.userStatisticsService = userStatisticsService;
        this.webCertUserService = webCertUserService;
        this.hsaOrganizationsService = hsaOrganizationsService;
    }

    @Override
    public ListConfig get(String unitId) {
        return getListConfig(unitId);
    }

    @Override
    public ListConfig update(ListConfig config, String unitId) {
        updateUnitFilter(config, unitId);
        updateSignedByFilter(config, unitId);
        config.setSecondaryTitle(getSecondaryTitle(unitId));
        return config;
    }

    private Optional<ListFilterConfig> getFilterFromId(List<ListFilterConfig> currentFilters, String id) {
        return currentFilters
                .stream()
                .filter((filter) -> filter.getId().equals(id))
                .findFirst();
    }

    private List<ListFilterConfig> removeFilter(ListConfig config, String unit) {
        final var currentFilters = config.getFilters();
        final Optional<ListFilterConfig> unitFilter = getFilterFromId(currentFilters, unit);
        unitFilter.ifPresent(currentFilters::remove);
        return currentFilters;
    }

    private void updateUnitFilter(ListConfig config, String unitId) {
        final List<ListFilterConfig> currentFilters = removeFilter(config, "UNIT");
        currentFilters.add(0, getUnitSelect(unitId));
    }


    private void updateSignedByFilter(ListConfig config, String unitId) {
        final List<ListFilterConfig> currentFilters = removeFilter(config, "SIGNED_BY");
        currentFilters.add(4, getSignedByFilter(unitId));
    }

    private ListConfig getListConfig(String unit) {
        final var config = new ListConfig();
        config.setTitle(TITLE);
        config.setSecondaryTitle(getSecondaryTitle(unit));
        config.setFilters(getFilters(unit));
        config.addButtonTooltip(CertificateListItemValueType.OPEN_BUTTON.toString(), OPEN_CERTIFICATE_TOOLTIP);
        config.addButtonTooltip(CertificateListItemValueType.SEARCH_BUTTON.toString(), SEARCH_CERTIFICATE_TOOLTIP);
        config.addButtonTooltip(CertificateListItemValueType.RESET_BUTTON.toString(), RESET_FILTER_TOOLTIP);
        config.setTableHeadings(getTableHeadings());
        config.setDescription(DESCRIPTION);
        config.setEmptyListText(EMPTY_LIST_TEXT);
        config.setShouldUpdateConfigAfterListSearch(true);
        return config;
    }

    private String getSecondaryTitle(String unitId) {
        if (unitId.length() == 0) {
            return "Ärenden visas för alla enheter";
        }
        final var unit = hsaOrganizationsService.getVardenhet(unitId);
        return "Ärenden visas för " + unit.getNamn();
    }

    public TableHeading[] getTableHeadings() {
        return new TableHeading[] {
                TableHeadingFactory.text(ListColumnType.QUESTION_ACTION),
                TableHeadingFactory.text(ListColumnType.SENDER),
                TableHeadingFactory.patientInfo(ListColumnType.PATIENT_ID),
                TableHeadingFactory.text(ListColumnType.SIGNED_BY),
                TableHeadingFactory.date(ListColumnType.SENT_RECEIVED, false),
                TableHeadingFactory.forwarded(ListColumnType.FORWARDED, "Visar om ärendet är vidarebefordrat."),
                TableHeadingFactory.forwardButton(ListColumnType.FORWARD_CERTIFICATE),
                TableHeadingFactory.openButton(ListColumnType.OPEN_CERTIFICATE)
        };
    }

    private List<ListFilterConfig> getFilters(String unit) {
        final var filters = new ArrayList<ListFilterConfig>();
        filters.add(getUnitSelect(unit));
        filters.add(ListFilterConfigFactory.forwardedSelect());
        filters.add(ListFilterConfigFactory.questionStatusSelect());
        filters.add(ListFilterConfigFactory.senderSelect());
        filters.add(getSignedByFilter(unit));
        filters.add(ListFilterConfigFactory.defaultPersonId());
        filters.add(ListFilterConfigFactory.sentDateRange());
        filters.add(ListFilterConfigFactory.orderBy(ListColumnType.SENT_RECEIVED));
        filters.add(ListFilterConfigFactory.ascending());
        filters.add(ListFilterConfigFactory.pageSize());
        return filters;
    }

    private ListFilterConfig getSignedByFilter(String unit) {
        final var savedByList = getStaffInfoFacadeService.get(unit);
        final var defaultValue = getStaffInfoFacadeService.getLoggedInStaffHsaId();
        return ListFilterConfigFactory.createStaffSelect("SIGNED_BY", "Signerat av", savedByList, defaultValue);
    }

    private ListFilterSelectConfig getUnitSelect(String unitId) {
        return new ListFilterSelectConfig("UNIT", "Enhet", getUnitList(unitId));
    }

    private List<ListFilterConfigValue> getUnitList(String unitId) {
        final var loggedInUnit = webCertUserService.getUser().getValdVardenhet();
        final var statistics = userStatisticsService.getUserStatistics();
        final var subUnits = hsaOrganizationsService.getVardenhet(loggedInUnit.getId()).getMottagningar();

        final var list = statistics.getUnitStatistics()
                .entrySet()
                .stream()
                .filter(
                        (unit) -> subUnits
                                .stream()
                                .map(AbstractVardenhet::getId)
                                .anyMatch(
                                        (subUnitId) -> isMatchedUnit(
                                                subUnitId, unit.getKey()) || isMatchedUnit(loggedInUnit.getId(), unit.getKey()
                                        )
                                )
                )
                .sorted(sortUnitFirstAndSubUnitsAlphabetical(loggedInUnit.getId()))
                .map(
                        (unit) -> getUnitSelectOption(unit.getKey(), unit.getValue())
                )
                .collect(Collectors.toList());

        list.add(0, ListFilterConfigValue.create("", getShowAllText(loggedInUnit.getId(), statistics), true));

        return list;
    }

    private String getShowAllText(String unitId, UserStatisticsDTO statistics) {
        final var unitStatistic = statistics.getUnitStatistics().get(unitId);
        final var totalQuestions = unitStatistic.getQuestionsOnUnit() + unitStatistic.getQuestionsOnSubUnits();
        return "Visa alla (" + totalQuestions + ")";
    }

    private Comparator<Map.Entry<String, UnitStatisticsDTO>> sortUnitFirstAndSubUnitsAlphabetical(String unitId) {
        return (o1, o2) -> isMatchedUnit(o1.getKey(), unitId) ? -1
                : isMatchedUnit(o2.getKey(), unitId) ? 1 : o1.getKey().compareTo(o2.getKey());
    }

    private boolean isMatchedUnit(String subUnitId, String unitId) {
        return subUnitId.equals(unitId);
    }


    private ListFilterConfigValue getUnitSelectOption(String unitId, UnitStatisticsDTO unit) {
        return ListFilterConfigValue.create(unitId, getUnitText(unitId, unit.getQuestionsOnUnit()), false);
    }

    private String getUnitText(String unitId, long nbrOfQuestions) {
        final var user = webCertUserService.getUser();
        final var selectedUnit = user.getValdVardenhet().getId();
        final var isSubUnit = !isMatchedUnit(unitId, selectedUnit);
        final var text = hsaOrganizationsService.getVardenhet(unitId).getNamn() + " (" + nbrOfQuestions + ')';
        return isSubUnit ? "&emsp; " + text : text;
    }

}
