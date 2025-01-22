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

public class TableHeading {

    private ListColumnType id;
    private String title;
    private CertificateListItemValueType type;
    private String description;
    private boolean defaultAscending;


    public TableHeading(ListColumnType id, String title, CertificateListItemValueType type, String description) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.description = description;
        this.defaultAscending = true;
    }

    public TableHeading(ListColumnType id, String title, CertificateListItemValueType type, String description, boolean defaultAscending) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.description = description;
        this.defaultAscending = defaultAscending;
    }

    public TableHeading() {

    }

    public ListColumnType getId() {
        return id;
    }

    public void setId(ListColumnType id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CertificateListItemValueType getType() {
        return type;
    }

    public void setType(CertificateListItemValueType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDefaultAscending() {
        return defaultAscending;
    }

    public void setDefaultAscending(boolean defaultAscending) {
        this.defaultAscending = defaultAscending;
    }
}
