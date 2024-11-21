/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import java.time.LocalDateTime;

public class ListFilterDateConfig extends ListFilterConfig {

    private LocalDateTime max;
    private LocalDateTime min;
    private LocalDateTime defaultValue;

    public ListFilterDateConfig(String id, String title) {
        super(ListFilterType.DATE, id, title);
    }

    public ListFilterDateConfig(String id, String title, LocalDateTime defaultValue) {
        super(ListFilterType.DATE, id, title);
        this.defaultValue = defaultValue;
    }

    public ListFilterDateConfig(String id, String title, LocalDateTime max, LocalDateTime min, LocalDateTime defaultValue) {
        super(ListFilterType.DATE, id, title);
        this.max = max;
        this.min = min;
        this.defaultValue = defaultValue;
    }

    public ListFilterDateConfig() {

    }

    public LocalDateTime getMax() {
        return max;
    }

    public void setMax(LocalDateTime max) {
        this.max = max;
    }

    public LocalDateTime getMin() {
        return min;
    }

    public void setMin(LocalDateTime min) {
        this.min = min;
    }

    public LocalDateTime getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(LocalDateTime defaultValue) {
        this.defaultValue = defaultValue;
    }
}
