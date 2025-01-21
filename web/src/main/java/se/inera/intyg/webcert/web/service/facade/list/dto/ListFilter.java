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
package se.inera.intyg.webcert.web.service.facade.list.dto;

import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterValue;

import java.util.HashMap;
import java.util.Map;

public class ListFilter {

    private Map<String, ListFilterValue> values;

    public ListFilter() {
        values = new HashMap<>();
    }

    public ListFilter(Map<String, ListFilterValue> values) {
        this.values = values;
    }

    public Map<String, ListFilterValue> getValues() {
        return values;
    }

    public void setValues(Map<String, ListFilterValue> values) {
        this.values = values;
    }

    public void addValue(ListFilterValue value, String key) {
        values.put(key, value);
    }

    public ListFilterValue getValue(String id) {
        if (values.containsKey(id)) {
            return values.get(id);
        }
        return null;
    }
}
