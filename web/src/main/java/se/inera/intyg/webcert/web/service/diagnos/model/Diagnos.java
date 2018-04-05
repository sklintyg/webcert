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
package se.inera.intyg.webcert.web.service.diagnos.model;

import java.util.Objects;

public class Diagnos implements Comparable<Diagnos> {

    private String kod;

    private String beskrivning;

    public String getKod() {
        return kod;
    }

    public void setKod(String kod) {
        this.kod = kod;
    }

    public String getBeskrivning() {
        return beskrivning;
    }

    public void setBeskrivning(String beskrivning) {
        this.beskrivning = beskrivning;
    }

    @Override
    public int compareTo(Diagnos d) {
        return getKod().compareTo(d.getKod());
    }

    @Override
    public String toString() {
        return kod + ":" + beskrivning;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof Diagnos)) {
            return false;
        } else {
            Diagnos d = (Diagnos) o;
            return Objects.equals(this.kod, d.kod) && Objects.equals(this.beskrivning, d.beskrivning);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.kod, this.beskrivning);
    }
}
