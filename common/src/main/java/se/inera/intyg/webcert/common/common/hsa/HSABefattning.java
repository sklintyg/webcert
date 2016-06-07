/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.common.common.hsa;

public enum HSABefattning {

    // Överläkare
    OVERLAKARE("201010", "Överläkare"),

    // Distriktsläkare/Specialist allmänmedicin
    DISTRIKTSlAKARE("201011", "Distriktsläkare/Specialist allmänmedicin"),

    // Skolläkare
    SKOLLAKARE("201012", "Skolläkare"),

    // Företagsläkare
    FORETAGSLAKARE("201013", "Företagsläkare"),

    // Specialistläkare
    SPECIALISTLAKARE("202010", "Specialistläkare"),

    // Legitimerad läkare under specialiseringstjänstgöring (STläkare)
    LAKARE_LEG_ST("203010", "Läkare legitimerad, specialiseringstjänstgöring"),

    // Legitimerad läkare under till exempel vikariat
    LAKARE_LEG_ANNAN("203090", "Läkare legitimerad, annan"),

    // Ej legitimerad läkare under allmäntjänstgöring (AT-läkare)
    LAKARE_EJ_LEG_AT("204010", "Läkare ej legitimerad, allmäntjänstgöring"),

    // Ej legitimerad läkare under till exempel vikariat eller provtjänstgöring
    LAKARE_EJ_LEG_ANNAN("204090", "Läkare ej legitimerad, annan");

    private String code;

    private String namn;

    HSABefattning(String code, String namn) {
        this.code = code;
        this.namn = namn;
    }

    public String getCode() {
        return code;
    }

    public String getNamn() {
        return namn;
    }
}
