/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.subscription;

import java.util.stream.Stream;

public enum AuthenticationMethodEnum {
    ELEG("ELEG", "Authentication method ELEG"),
    SITHS("SITHS", "Authentication method SITHS");

    private final String value;
    private final String description;

    AuthenticationMethodEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String value() {
        return this.value;
    }

    public String description() {
        return this.description;
    }

    public static AuthenticationMethodEnum fromValue(String value) {
        return Stream.of(values()).filter((s) -> value.equals(s.value())).findFirst()
            .orElseThrow(() -> new IllegalArgumentException(value));
    }
}