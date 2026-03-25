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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UnitAddressConverterTest {

  private static final String ZIP_CODE = "12345";
  private static final String CITY = "City";
  private static final List<String> ADDRESS_LINES = List.of("1", "2", "12345 City");
  private static final List<String> ADDRESS_LINES_NO_ZIP_OR_CITY = List.of("1", "2", "3");
  private static final String EXPECTED_ADDRESS = "1 2";
  private final UnitAddressConverter converter = new UnitAddressConverter();

  @Nested
  class Address {

    @Test
    void shouldReturnNullIfLinesAreNull() {
      final var response = converter.convertAddress(null);

      assertNull(response);
    }

    @Test
    void shouldReturnEmptyIfLinesAreEmpty() {
      final var response = converter.convertAddress(Collections.emptyList());

      assertTrue(response.isEmpty());
    }

    @Test
    void shouldReturnAllButLastLines() {
      final var response = converter.convertAddress(ADDRESS_LINES);

      assertEquals(EXPECTED_ADDRESS, response);
    }
  }

  @Nested
  class ZipCode {

    @Test
    void shouldReturnFromValueIfDefined() {
      final var response = converter.convertZipCode(ADDRESS_LINES, "zip");

      assertEquals("zip", response);
    }

    @Test
    void shouldReturnExtractedZipCodeIfNoValueZipCode() {
      final var response = converter.convertZipCode(ADDRESS_LINES, null);

      assertEquals(ZIP_CODE, response);
    }

    @Test
    void shouldReturnEmptyIfNoZipCodeInAddressLines() {
      final var response = converter.convertZipCode(ADDRESS_LINES_NO_ZIP_OR_CITY, null);

      assertEquals("", response);
    }
  }

  @Nested
  class City {

    @Test
    void shouldReturnExtractedIfIncludedInAddressLines() {
      final var response = converter.convertCity(ADDRESS_LINES);

      assertEquals(CITY, response);
    }

    @Test
    void shouldReturnLastLineIfWrongFormat() {
      final var response = converter.convertCity(ADDRESS_LINES_NO_ZIP_OR_CITY);

      assertEquals(ADDRESS_LINES_NO_ZIP_OR_CITY.get(2), response);
    }
  }
}
