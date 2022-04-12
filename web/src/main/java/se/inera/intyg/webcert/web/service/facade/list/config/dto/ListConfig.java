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

package se.inera.intyg.webcert.web.service.facade.list.config.dto;

import java.util.List;

public class ListConfig {

    private List<ListFilterConfig> filters;
    private String title;
    private String description;
    private String emptyListText;
    private String secondaryTitle;
    private List<Integer> pageSizes;
    private String openCertificateTooltip;
    private String searchCertificateTooltip;
    private TableHeading[] tableHeadings;

    public ListConfig(){}

    public ListConfig(List<ListFilterConfig> filters, String title, String description, String emptyListText, String secondaryTitle, List<Integer> pageSizes, String openCertificateTooltip, String searchCertificateTooltip, TableHeading[] tableHeadings) {
        this.filters = filters;
        this.title = title;
        this.description = description;
        this.emptyListText = emptyListText;
        this.secondaryTitle = secondaryTitle;
        this.pageSizes = pageSizes;
        this.openCertificateTooltip = openCertificateTooltip;
        this.searchCertificateTooltip = searchCertificateTooltip;
        this.tableHeadings = tableHeadings;
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

    public String getOpenCertificateTooltip() {
        return openCertificateTooltip;
    }

    public void setOpenCertificateTooltip(String openCertificateTooltip) {
        this.openCertificateTooltip = openCertificateTooltip;
    }

    public TableHeading[] getTableHeadings() {
        return tableHeadings;
    }

    public void setTableHeadings(TableHeading[] tableHeadings) {
        this.tableHeadings = tableHeadings;
    }


    public String getSearchCertificateTooltip() {
        return searchCertificateTooltip;
    }

    public void setSearchCertificateTooltip(String searchCertificateTooltip) {
        this.searchCertificateTooltip = searchCertificateTooltip;
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
}
