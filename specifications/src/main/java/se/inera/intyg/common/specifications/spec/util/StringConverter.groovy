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

package se.inera.intyg.common.specifications.spec.util

import fitnesse.slim.Converter;

public class StringConverter implements Converter<String> {

    private static final String NULL = "<null>";
    private static final String EMPTY = "<empty>";
    private static final String SPACE = "<space>";
    
    @Override
    public String fromString(String inputString) {
        if (inputString == null || inputString.isEmpty() || EMPTY.equals(inputString)) {
            return "";
        }
        if (SPACE.equals(inputString)) {
            return " ";
        }
        if (NULL.equals(inputString)) {
            return null;
        }
        return inputString.trim();
    }

    @Override
    public String toString(String obj) {
        if (obj == null) {
            return NULL;
        }
        if (" ".equals(obj)) {
            return SPACE;
        }
        if ("".equals(obj)) {
            return EMPTY;
        }
        return obj.trim();
    }
}
