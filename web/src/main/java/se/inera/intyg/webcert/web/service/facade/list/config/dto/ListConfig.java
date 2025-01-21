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
package se.inera.intyg.webcert.web.service.facade.list.config.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListConfig {

    private List<ListFilterConfig> filters;
    private String title;
    private String description;
    private String emptyListText;
    private String secondaryTitle;
    private List<Integer> pageSizes;
    private Map<String, String> buttonTooltips;
    private boolean excludeFilterButtons;
    private TableHeading[] tableHeadings;
    private boolean shouldUpdateConfigAfterListSearch;

    public ListConfig() {
    }

    public ListConfig(List<ListFilterConfig> filters, String title, String description, String emptyListText,
        String secondaryTitle, List<Integer> pageSizes, TableHeading[] tableHeadings,
        boolean shouldUpdateConfigAfterListSearch) {
        this.filters = filters;
        this.title = title;
        this.description = description;
        this.emptyListText = emptyListText;
        this.secondaryTitle = secondaryTitle;
        this.pageSizes = pageSizes;
        this.tableHeadings = tableHeadings;
        this.shouldUpdateConfigAfterListSearch = shouldUpdateConfigAfterListSearch;
        buttonTooltips = new HashMap<>();
    }

    public List<ListFilterConfig> getFilters() {
        return filters;
    }

    public void setFilters(List<ListFilterConfig> filters) {
        this.filters = filters;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Integer> getPageSizes() {
        return pageSizes;
    }

    public void setPageSizes(List<Integer> pageSizes) {
        this.pageSizes = pageSizes;
    }

    public TableHeading[] getTableHeadings() {
        return tableHeadings;
    }

    public void setTableHeadings(TableHeading[] tableHeadings) {
        this.tableHeadings = tableHeadings;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmptyListText() {
        return emptyListText;
    }

    public void setEmptyListText(String emptyListText) {
        this.emptyListText = emptyListText;
    }

    public String getSecondaryTitle() {
        return secondaryTitle;
    }

    public void setSecondaryTitle(String secondaryTitle) {
        this.secondaryTitle = secondaryTitle;
    }

    public boolean isExcludeFilterButtons() {
        return excludeFilterButtons;
    }

    public void setExcludeFilterButtons(boolean excludeFilterButtons) {
        this.excludeFilterButtons = excludeFilterButtons;
    }

    public Map<String, String> getButtonTooltips() {
        return buttonTooltips;
    }

    public void addButtonTooltip(String key, String value) {
        if (buttonTooltips == null) {
            buttonTooltips = new HashMap<>();
        }
        buttonTooltips.put(key, value);
    }

    public boolean isShouldUpdateConfigAfterListSearch() {
        return shouldUpdateConfigAfterListSearch;
    }

    public void setShouldUpdateConfigAfterListSearch(boolean shouldUpdateConfigAfterListSearch) {
        this.shouldUpdateConfigAfterListSearch = shouldUpdateConfigAfterListSearch;
    }
}
