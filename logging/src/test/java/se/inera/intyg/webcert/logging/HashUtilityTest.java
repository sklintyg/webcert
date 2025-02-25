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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class HashUtilityTest {
    private final HashUtility hashUtility = new HashUtility();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(hashUtility, "salt", "salt");
    }

    @Test
    void shouldReturnHashedValue() {
        final var payload = "123123123";
        final var hashedPayload = hashUtility.hash(payload);
        assertEquals("f0b9a3394c4a24871d26ed9e0b7e81dc08714204caafe9821e7fb141ae410286", hashedPayload);
    }

    @Test
    void shouldReturnEmptyHashConstantWhenPayloadIsNull() {
        final var hashedPayload = hashUtility.hash(null);
        assertEquals(HashUtility.EMPTY, hashedPayload);
    }

    @Test
    void shouldReturnEmptyHashConstantWhenPayloadIsEmpty() {
        final var payload = "";
        final var hashedPayload = hashUtility.hash(payload);
        assertEquals(HashUtility.EMPTY, hashedPayload);
    }
}
