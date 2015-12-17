/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.common.common.security.authority;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Created by Magnus Ekstrand on 27/08/15.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserPrivilege {

    PRIVILEGE_SIGNERA_INTYG ("Signera intyg"),
    PRIVILEGE_MAKULERA_INTYG ("Makulera intyg"),
    PRIVILEGE_KOPIERA_INTYG ("Kopiera intyg"),
    PRIVILEGE_VIDAREBEFORDRA_UTKAST ("Vidarebefordra utkast"),
    PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR ("Vidarebefordra frågasvar"),
    PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA ("Besvara fråga om komplettering"),
    PRIVILEGE_FILTRERA_PA_LAKARE ("Filtrera på annan läkare"),
    PRIVILEGE_ATKOMST_ANDRA_ENHETER ("Åtkomst andra vårdenheter"),
    PRIVILEGE_HANTERA_PERSONUPPGIFTER ("Hantera personuppgifter"),
    PRIVILEGE_HANTERA_MAILSVAR ("Hantera notifieringsmail om frågasvar"),
    PRIVILEGE_NAVIGERING ("Navigera i menyer, på logo, tillbakaknappar");

    private final String text;

    UserPrivilege(String text) {
        this.text = text;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name().equals(otherName);
    }

    public boolean equalsText(String otherText) {
        return (otherText == null) ? false : text.equals(otherText);
    }

    public String text() {
        return this.text;
    }

    public String toString() {
        return this.text;
    }
}
