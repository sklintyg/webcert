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

package se.inera.intyg.webcert.web.logging;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HashPatientIdHelperTest {

    @Test
    void testHashPatientIdAtEnd() {
        final var url = "api/test/191212121212";
        final var hashedUrl = HashPatientIdHelper.fromUrl(url);

        assertAll(
            () -> assertNotEquals(url, hashedUrl),
            () -> assertTrue(hashedUrl.startsWith("api/test/")),
            () -> assertFalse(hashedUrl.contains("191212121212"))
        );
    }

    @Test
    void testHashPatientIdInMiddle() {
        final var url = "api/191212121212/api";
        final var hashedUrl = HashPatientIdHelper.fromUrl(url);

        assertAll(
            () -> assertNotEquals(url, hashedUrl),
            () -> assertTrue(hashedUrl.startsWith("api/")),
            () -> assertTrue(hashedUrl.endsWith("/api")),
            () -> assertFalse(hashedUrl.contains("191212121212"))
        );
    }

    @Test
    void testHashPatientIdNotFound() {
        final var url = "api/test/noidhere";
        final var hashedUrl = HashPatientIdHelper.fromUrl(url);
        assertEquals(url, hashedUrl);
    }
}
