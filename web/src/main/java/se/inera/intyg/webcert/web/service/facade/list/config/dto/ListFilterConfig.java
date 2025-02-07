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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ListFilterTextConfig.class, name = "TEXT"),
    @JsonSubTypes.Type(value = ListFilterPersonIdConfig.class, name = "PERSON_ID"),
    @JsonSubTypes.Type(value = ListFilterDateRangeConfig.class, name = "DATE_RANGE"),
    @JsonSubTypes.Type(value = ListFilterSelectConfig.class, name = "SELECT"),
    @JsonSubTypes.Type(value = ListFilterRadioConfig.class, name = "RADIO"),
    @JsonSubTypes.Type(value = ListFilterOrderConfig.class, name = "ORDER"),
    @JsonSubTypes.Type(value = ListFilterBooleanConfig.class, name = "BOOLEAN"),
    @JsonSubTypes.Type(value = ListFilterSelectConfig.class, name = "SELECT"),
    @JsonSubTypes.Type(value = ListFilterDateConfig.class, name = "DATE"),
    @JsonSubTypes.Type(value = ListFilterPageSizeConfig.class, name = "PAGESIZE")
})

public class ListFilterConfig {

    private ListFilterType type;
    private String id;
    private String title;
    private boolean alwaysHighlighted;
    private String description;

    public ListFilterConfig(ListFilterType type, String id, String title, boolean alwaysHighlighted) {
        this(type, id, title, alwaysHighlighted, "");
    }

    public ListFilterConfig(ListFilterType type, String id, String title) {
        this(type, id, title, false, "");
    }

    public ListFilterConfig(ListFilterType type, String id, String title, boolean alwaysHighlighted, String description) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.alwaysHighlighted = alwaysHighlighted;
        this.description = description;
    }

    public ListFilterConfig() {

    }

    public ListFilterType getType() {
        return type;
    }

    public void setType(ListFilterType type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isAlwaysHighlighted() {
        return alwaysHighlighted;
    }

    public void setAlwaysHighlight(boolean alwaysHighlighted) {
        this.alwaysHighlighted = alwaysHighlighted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
