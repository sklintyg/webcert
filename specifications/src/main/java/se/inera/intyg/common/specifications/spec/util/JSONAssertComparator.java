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

package se.inera.intyg.common.specifications.spec.util;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import fitnesse.testsystems.slim.CustomComparator;

public class JSONAssertComparator implements CustomComparator {
    @Override
    public boolean matches(String actual, String expected) {
        try {
            JSONAssert.assertEquals(expected, actual, false);
            return true;
        } catch (JSONException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
