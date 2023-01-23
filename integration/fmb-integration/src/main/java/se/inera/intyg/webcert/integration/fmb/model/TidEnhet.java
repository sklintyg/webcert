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
package se.inera.intyg.webcert.integration.fmb.model;

import static com.google.common.collect.MoreCollectors.toOptional;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;

import java.util.Optional;
import java.util.stream.Stream;

public enum TidEnhet {
    DAG("d", 1, "dag", "dagar"),
    VECKA("wk", 7, "vecka", "veckor"),
    MANAD("mo", 31, "månad", "månader");

    private final String code;
    private final int inDays;
    private final String singleName;
    private final String pluralisName;

    TidEnhet(final String code, final int inDays, final String singleName, final String pluralisName) {
        this.code = code;
        this.inDays = inDays;
        this.singleName = singleName;
        this.pluralisName = pluralisName;
    }

    public static Optional<TidEnhet> of(final String text) {
        return Stream.of(TidEnhet.values())
            .filter(code -> equalsIgnoreCase(text, code.getCode()))
            .collect(toOptional());
    }

    public String getUnitDisplayValue(Integer value) {
        if (value == null) {
            return "";
        }
        return value + " " + (value > 1 ? pluralisName : singleName);
    }

    public String getCode() {
        return code;
    }

    public int getInDays() {
        return inDays;
    }
}
