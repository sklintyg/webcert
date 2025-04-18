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

import java.util.List;

public class ListFilterSelectConfig extends ListFilterConfig {

    private List<ListFilterConfigValue> values;

    public ListFilterSelectConfig(String id, String title, List<ListFilterConfigValue> values) {
        super(ListFilterType.SELECT, id, title);
        this.values = values;
    }

    public ListFilterSelectConfig(String id, String title, List<ListFilterConfigValue> values, boolean alwaysHighlighted) {
        super(ListFilterType.SELECT, id, title, alwaysHighlighted);
        this.values = values;
    }

    public ListFilterSelectConfig() {

    }

    public List<ListFilterConfigValue> getValues() {
        return values;
    }

    public void setValues(List<ListFilterConfigValue> values) {
        this.values = values;
    }
}
