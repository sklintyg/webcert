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

package se.inera.intyg.webcert.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HashUtilityTest {

    @Test
    void shouldReturnHashedValue() {
        final var payload = "123123123";
        final var hashedPayload = HashUtility.hash(payload);
        assertEquals("932f3c1b56257ce8539ac269d7aab42550dacf8818d075f0bdf1990562aae3ef", hashedPayload);
    }

    @Test
    void shouldReturnEmptyHashConstantWhenPayloadIsNull() {
        final var hashedPayload = HashUtility.hash(null);
        assertEquals(HashUtility.EMPTY, hashedPayload);
    }

    @Test
    void shouldReturnEmptyHashConstantWhenPayloadIsEmpty() {
        final var payload = "";
        final var hashedPayload = HashUtility.hash(payload);
        assertEquals(HashUtility.EMPTY, hashedPayload);
    }
}
