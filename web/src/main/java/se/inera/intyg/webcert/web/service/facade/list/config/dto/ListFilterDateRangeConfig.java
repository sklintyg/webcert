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

public class ListFilterDateRangeConfig extends ListFilterConfig {

    private ListFilterDateConfig to;
    private ListFilterDateConfig from;
    private boolean forbidFutureDates;

    public ListFilterDateRangeConfig(String id, String title, ListFilterDateConfig to,
        ListFilterDateConfig from, boolean forbidFutureDates) {
        super(ListFilterType.DATE_RANGE, id, title);
        this.to = to;
        this.from = from;
        this.forbidFutureDates = forbidFutureDates;
    }

    public ListFilterDateRangeConfig(String id, String title, ListFilterDateConfig to,
        ListFilterDateConfig from, boolean forbidFutureDates, String description) {
        super(ListFilterType.DATE_RANGE, id, title, false, description);
        this.to = to;
        this.from = from;
        this.forbidFutureDates = forbidFutureDates;
    }

    public ListFilterDateRangeConfig() {

    }

    public ListFilterDateConfig getTo() {
        return to;
    }

    public void setTo(ListFilterDateConfig to) {
        this.to = to;
    }

    public ListFilterDateConfig getFrom() {
        return from;
    }

    public void setFrom(ListFilterDateConfig from) {
        this.from = from;
    }

    public boolean isForbidFutureDates() {
        return forbidFutureDates;
    }

    public void setForbidFutureDates(boolean forbidFutureDates) {
        this.forbidFutureDates = forbidFutureDates;
    }
}
