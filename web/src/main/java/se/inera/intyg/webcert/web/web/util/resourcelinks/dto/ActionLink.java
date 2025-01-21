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
package se.inera.intyg.webcert.web.web.util.resourcelinks.dto;

import java.util.Objects;

/**
 * DTO for available actions.
 */
public class ActionLink {

    private ActionLinkType type;

    /**
     * Empty action link. Type will be null.
     */
    public ActionLink() {

    }

    /**
     * New action link.
     *
     * @param type Type of action link.
     */
    public ActionLink(ActionLinkType type) {
        this.type = type;
    }

    /**
     * Get the type of the link.
     *
     * @return Type of link. Can be null.
     */
    public ActionLinkType getType() {
        return type;
    }

    /**
     * Set the type of the link.
     *
     * @param type Type of link.
     */
    public void setType(ActionLinkType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActionLink that = (ActionLink) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
