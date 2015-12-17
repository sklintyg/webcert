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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Magnus Ekstrand on 27/08/15.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserRole {

    ROLE_VARDADMINISTRATOR ("Vårdadministratör", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD ("Vårdadministratör", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_VARDADMINISTRATOR_UTHOPP ("Vårdadministratör", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_LAKARE ("Läkare", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_LAKARE_DJUPINTEGRERAD ("Läkare", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_LAKARE_UTHOPP ("Läkare", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_PRIVATLAKARE ("Privatläkare", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_TANDLAKARE ("Tandläkare", "fk7263"),
    ROLE_TANDLAKARE_DJUPINTEGRERAD ("Tandläkare", "fk7263"),
    ROLE_TANDLAKARE_UTHOPP ("Tandläkare", "fk7263");

    private String text;
    private Set<String> authorizedIntygsTyper;

    UserRole(String text, String... intygsTyper) {
        this.text = text;
        authorizedIntygsTyper = new HashSet<>();

        Collections.addAll(authorizedIntygsTyper, intygsTyper);
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name().equals(otherName);
    }

    public boolean equalsText(String otherText) {
        return (otherText == null) ? false : text.equals(otherText);
    }

    public Set<String> getAuthorizedIntygsTyper() {
        return authorizedIntygsTyper;
    }

    public String text() {
        return this.text;
    }

    /**
     * This is only to make serialization of enum using Jackson Shape.OBJECT to work properly.
     */
    public String getName() {
        return this.text;
    }

    public String toString() {
        return this.text;
    }

}
