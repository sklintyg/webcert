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
package se.inera.intyg.webcert.web.service.dto;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class LakareTest {

    @Test
    public void testMerge() {
        Lakare lakare1 = new Lakare("1", "1");
        Lakare lakare2 = new Lakare("2", "2");
        Lakare lakare3 = new Lakare("3", "3");
        Lakare lakare4 = new Lakare("4", "4");
        List<Lakare> a = Arrays.asList(lakare1, lakare2, lakare4);
        List<Lakare> b = Arrays.asList(lakare2, lakare3, lakare4);
        List<Lakare> merged = Lakare.merge(a, b);
        assertNotNull(merged);
        assertFalse(merged.isEmpty());
        assertEquals(Arrays.asList(lakare1, lakare2, lakare3, lakare4), merged);
    }
}
