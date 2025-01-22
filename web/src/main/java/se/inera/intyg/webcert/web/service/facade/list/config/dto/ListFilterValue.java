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
    @JsonSubTypes.Type(value = ListFilterTextValue.class, name = "TEXT"),
    @JsonSubTypes.Type(value = ListFilterPersonIdValue.class, name = "PERSON_ID"),
    @JsonSubTypes.Type(value = ListFilterDateRangeValue.class, name = "DATE_RANGE"),
    @JsonSubTypes.Type(value = ListFilterSelectValue.class, name = "SELECT"),
    @JsonSubTypes.Type(value = ListFilterRadioValue.class, name = "RADIO"),
    @JsonSubTypes.Type(value = ListFilterTextValue.class, name = "ORDER"),
    @JsonSubTypes.Type(value = ListFilterBooleanValue.class, name = "BOOLEAN"),
    @JsonSubTypes.Type(value = ListFilterNumberValue.class, name = "NUMBER")
})
public interface ListFilterValue {

    ListFilterType getType();
}
