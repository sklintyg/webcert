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

package se.inera.intyg.webcert.web.service.mail.stubs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(
    ignoreUnknown = true
)
public class FakeLogins {
    private String forvaldEnhet;
    private String beskrivning;

    public FakeLogins() {
    }

    public String getForvaldEnhet() {
        return this.forvaldEnhet;
    }

    public String getBeskrivning() {
        return this.beskrivning;
    }

    public void setForvaldEnhet(String forvaldEnhet) {
        this.forvaldEnhet = forvaldEnhet;
    }

    public void setBeskrivning(String beskrivning) {
        this.beskrivning = beskrivning;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof FakeLogins)) {
            return false;
        } else {
            FakeLogins other = (FakeLogins)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$forvaldEnhet = this.getForvaldEnhet();
                Object other$forvaldEnhet = other.getForvaldEnhet();
                if (this$forvaldEnhet == null) {
                    if (other$forvaldEnhet != null) {
                        return false;
                    }
                } else if (!this$forvaldEnhet.equals(other$forvaldEnhet)) {
                    return false;
                }

                Object this$beskrivning = this.getBeskrivning();
                Object other$beskrivning = other.getBeskrivning();
                if (this$beskrivning == null) {
                    if (other$beskrivning != null) {
                        return false;
                    }
                } else if (!this$beskrivning.equals(other$beskrivning)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof FakeLogins;
    }

    public int hashCode() {
        int result = 1;
        Object $forvaldEnhet = this.getForvaldEnhet();
        result = result * 59 + ($forvaldEnhet == null ? 43 : $forvaldEnhet.hashCode());
        Object $beskrivning = this.getBeskrivning();
        result = result * 59 + ($beskrivning == null ? 43 : $beskrivning.hashCode());
        return result;
    }

    public String toString() {
        String var10000 = this.getForvaldEnhet();
        return "FakeLogins(forvaldEnhet=" + var10000 + ", beskrivning=" + this.getBeskrivning() + ")";
    }
}
