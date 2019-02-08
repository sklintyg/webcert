/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api.dto.icf;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class IcfKod {

    private String kod;
    private String benamning;
    private String beskrivning;
    private String innefattar;

    public IcfKod() {
    }

    IcfKod(final String kod, final String benamning, final String beskrivning, final String innefattar) {
        this.kod = kod;
        this.benamning = benamning;
        this.beskrivning = beskrivning;
        this.innefattar = innefattar;
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

    public String getBenamning() {
        return benamning;
    }

    public void setBenamning(final String benamning) {
        this.benamning = benamning;
    }

    public static IcfKod of(final String kod, final String benamning, final String beskrivning, final String innefattar) {
        return new IcfKod(kod, benamning, beskrivning, innefattar);
    }

    public String getInnefattar() {
        return innefattar;
    }

    public void setInnefattar(final String innefattar) {
        this.innefattar = innefattar;
    }

    // CHECKSTYLE:OFF NeedBraces
    // CHECKSTYLE:OFF MagicNumber
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (!(o instanceof IcfKod)) return false;

        final IcfKod icfKod = (IcfKod) o;

        return new EqualsBuilder()
                .append(kod, icfKod.kod)
                .append(benamning, icfKod.benamning)
                .append(beskrivning, icfKod.beskrivning)
                .append(innefattar, icfKod.innefattar)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(kod)
                .append(benamning)
                .append(beskrivning)
                .append(innefattar)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("kod", kod)
                .append("benamning", benamning)
                .append("beskrivning", beskrivning)
                .append("innefattar", innefattar)
                .toString();
    }
    // CHECKSTYLE:ON NeedBraces
    // CHECKSTYLE:ON MagicNumber
}
