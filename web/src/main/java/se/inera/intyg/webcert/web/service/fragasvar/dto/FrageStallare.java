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
package se.inera.intyg.webcert.web.service.fragasvar.dto;

public enum FrageStallare {

    FORSAKRINGSKASSAN("FK", "Försäkringskassan"),
    WEBCERT("WC", "Webcert");

    private final String kod;
    private final String name;

    FrageStallare(String kod, String name) {
        this.kod = kod;
        this.name = name;
    }

    public boolean isKodEqual(String kodValue) {
        return this.kod.equalsIgnoreCase(kodValue);
    }

    public boolean isNameEqual(String nameValue) {
        return this.name.equalsIgnoreCase(nameValue);
    }

    public String getKod() {
        return this.kod;
    }

    public String getName() {
        return name;
    }

    public static FrageStallare getByKod(String kodVal) {
        for (FrageStallare f : values()) {
            if (f.getKod().equals(kodVal)) {
                return f;
            }
        }
        return null;
    }
}
