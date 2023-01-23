/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.arende.model;

import java.util.Optional;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;

public enum ArendeAmne {
    AVSTMN("Avstämningsmöte"),
    KONTKT("Kontakt"),
    OVRIGT("Övrigt"),
    PAMINN("Påminnelse"),
    KOMPLT("Komplettering");

    private final String description;

    ArendeAmne(String code) {
        this.description = code;
    }

    public String getDescription() {
        return description;
    }

    public static Optional<ArendeAmne> fromAmne(Amne amne) {
        switch (amne) {
            case KOMPLETTERING_AV_LAKARINTYG:
                return Optional.of(KOMPLT);
            case AVSTAMNINGSMOTE:
                return Optional.of(AVSTMN);
            case KONTAKT:
                return Optional.of(KONTKT);
            case PAMINNELSE:
                return Optional.of(PAMINN);
            case OVRIGT:
                return Optional.of(OVRIGT);
            default:
                return Optional.empty();
        }
    }
}
