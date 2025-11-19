/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.utkast.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PreviousIntygTest {

    @Test
    public void testNotSameVardgivareWithHideValues() {
        PreviousIntyg dto = PreviousIntyg.of(false, true, false, "Enhet", "intygsId", null, true);

        assertFalse(dto.isSameVardgivare());
        assertNull(dto.getEnhetName());
        assertNull(dto.getLatestIntygsId());
        assertFalse(dto.isEnableShowDoiButton());
        assertFalse(dto.isSameEnhet());
    }

    @Test
    public void testNotSameVardgivareWithoutHideValues() {
        PreviousIntyg dto = PreviousIntyg.of(false, true, false, "Enhet", "intygsId", null, false);

        assertFalse(dto.isSameVardgivare());
        assertEquals("Enhet", dto.getEnhetName());
        assertEquals("intygsId", dto.getLatestIntygsId());
        assertFalse(dto.isEnableShowDoiButton());
        assertTrue(dto.isSameEnhet());
    }

    @Test
    public void testNotSameVardgivareDefaultBehavior() {
        PreviousIntyg dto = PreviousIntyg.of(false, true, false, "Enhet", "intygsId", null);

        assertFalse(dto.isSameVardgivare());
        assertNull(dto.getEnhetName());
        assertNull(dto.getLatestIntygsId());
        assertFalse(dto.isEnableShowDoiButton());
        assertFalse(dto.isSameEnhet());
    }

    @Test
    public void testSameVardgivareSameEnhet() {
        PreviousIntyg dto = PreviousIntyg.of(true, true, true, "Enhet", "intygsId", null);

        assertTrue(dto.isSameVardgivare());
        assertEquals("Enhet", dto.getEnhetName());
        assertEquals("intygsId", dto.getLatestIntygsId());
        assertTrue(dto.isEnableShowDoiButton());
        assertTrue(dto.isSameEnhet());
    }

    @Test
    public void testSameVardgivareNotSameEnhet() {
        PreviousIntyg dto = PreviousIntyg.of(true, false, false, "Enhet", "intygsId", null);

        assertTrue(dto.isSameVardgivare());
        assertEquals("Enhet", dto.getEnhetName());
        assertEquals("intygsId", dto.getLatestIntygsId());
        assertFalse(dto.isEnableShowDoiButton());
        assertFalse(dto.isSameEnhet());
    }
}
