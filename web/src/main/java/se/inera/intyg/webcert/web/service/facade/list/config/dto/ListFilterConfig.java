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

public class ListFilterConfig {
    private ListFilterType type;
    private String id;
    private String title;
    private boolean alwaysHighlighted;

    public ListFilterConfig(ListFilterType type, String id, String title) {
        this(type, id, title, false);
    }

    public ListFilterConfig(ListFilterType type, String id, String title, boolean alwaysHighlighted) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.alwaysHighlighted = alwaysHighlighted;
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
}
