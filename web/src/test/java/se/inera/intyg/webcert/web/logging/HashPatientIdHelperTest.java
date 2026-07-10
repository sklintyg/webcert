/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class HashPatientIdHelperTest {

  static Stream<String> patientIds() {
    return Stream.of(
        "191212121212",
        "19121212-1212",
        "19121212+1212",
        "1212121212",
        "121212-1212",
        "121212+1212");
  }

  @ParameterizedTest
  @MethodSource("patientIds")
  void testHashPatientIdAtEnd(String patientId) {
    final var url = "api/test/" + patientId;
    final var hashedUrl = HashPatientIdHelper.fromUrl(url);
    assertAll(
        () -> assertNotEquals(url, hashedUrl),
        () -> assertTrue(hashedUrl.startsWith("api/test/")),
        () -> assertFalse(hashedUrl.contains(patientId)));
  }

  @ParameterizedTest
  @MethodSource("patientIds")
  void testHashPatientIdInMiddle(String patientId) {
    final var url = "api/" + patientId + "/api";
    final var hashedUrl = HashPatientIdHelper.fromUrl(url);

    assertAll(
        () -> assertNotEquals(url, hashedUrl),
        () -> assertTrue(hashedUrl.startsWith("api/")),
        () -> assertTrue(hashedUrl.endsWith("/api")),
        () -> assertFalse(hashedUrl.contains(patientId)));
  }

  @Test
  void testHashPatientIdNotFound() {
    final var url = "api/test/noidhere";
    final var hashedUrl = HashPatientIdHelper.fromUrl(url);
    assertEquals(url, hashedUrl);
  }
}
