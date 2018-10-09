/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class IcfKod {

    private String kod;
    private String beskrivning;

    public IcfKod() {
    }

    private IcfKod(final String kod, final String beskrivning) {
        this.kod = kod;
        this.beskrivning = beskrivning;
    }

    public String getKod() {
        return kod;
    }

    public void setKod(final String kod) {
        this.kod = kod;
    }

    public String getBeskrivning() {
        return beskrivning;
    }

    public void setBeskrivning(final String beskrivning) {
        this.beskrivning = beskrivning;
    }

    public static IcfKod of(final String kod, final String beskrivning) {
        return new IcfKod(kod, beskrivning);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final IcfKod icfKod = (IcfKod) o;

        return new EqualsBuilder()
                .append(kod, icfKod.kod)
                .append(beskrivning, icfKod.beskrivning)
                .isEquals();
    }

    // CHECKSTYLE:OFF MagicNumber
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(kod)
                .append(beskrivning)
                .toHashCode();
    }
    // CHECKSTYLE:ON MagicNumber

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("kod", kod)
                .append("beskrivning", beskrivning)
                .toString();
    }
}
