/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api.dto.icf;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Created by marced on 2019-02-07.
 */
public class IcfKodTest {
    private static final String KOD = "kod";
    private static final String BENAMNING = "benamning";
    private static final String BESKRIVNING = "beskrivning";
    private static final String INNEFATTAR = "innefattar";

    @Test
    public void testEqualsWhenEqual() {
        IcfKod icfKod = new IcfKod(KOD, BENAMNING, BESKRIVNING, INNEFATTAR);
        IcfKod icfKod2 = new IcfKod(KOD, BENAMNING, BESKRIVNING, INNEFATTAR);
        IcfCentralKod icfCentralKod = new IcfCentralKod(KOD, BENAMNING, BESKRIVNING, INNEFATTAR);
        IcfKompletterandeKod icfKompletterandeKod = new IcfKompletterandeKod(KOD, BENAMNING, BESKRIVNING, INNEFATTAR);

        // Equals same class
        assertTrue(icfKod.equals(icfKod2));

        // Equals subclass (symmetric)
        assertTrue(icfKod.equals(icfCentralKod));
        assertTrue(icfCentralKod.equals(icfKod));

        assertTrue(icfKod.equals(icfKompletterandeKod));
        assertTrue(icfKompletterandeKod.equals(icfKod));

        // subclasses symmetric
        assertTrue(icfKompletterandeKod.equals(icfCentralKod));
        assertTrue(icfCentralKod.equals(icfKompletterandeKod));

    }

    @Test
    public void testNotEqualsWhenNotEqual() {
        IcfKod icfKod = new IcfKod(KOD, BENAMNING, BESKRIVNING, INNEFATTAR);
        IcfKod icfKod2 = new IcfKod(KOD, BENAMNING, BESKRIVNING, "");
        IcfCentralKod icfCentralKod = new IcfCentralKod(KOD, "", BESKRIVNING, INNEFATTAR);
        IcfKompletterandeKod icfKompletterandeKod = new IcfKompletterandeKod(KOD, BENAMNING, "", INNEFATTAR);

        // same class
        assertFalse(icfKod.equals(icfKod2));

        // super <--> subclass (symmetric)
        assertFalse(icfKod.equals(icfCentralKod));
        assertFalse(icfCentralKod.equals(icfKod));

        assertFalse(icfKod.equals(icfKompletterandeKod));
        assertFalse(icfKompletterandeKod.equals(icfKod));

        // subclasses symmetric
        assertFalse(icfKompletterandeKod.equals(icfCentralKod));
        assertFalse(icfCentralKod.equals(icfKompletterandeKod));

    }

}
