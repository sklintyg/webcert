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
package se.inera.intyg.webcert.web.service.facade.list.config.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListFilterDateRangeValueTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  void shouldDeserializeDateOnlyStringForFrom() throws Exception {
    final var json =
        """
        {"type":"DATE_RANGE","from":"2026-01-02","to":null}
        """;
    final var result = objectMapper.readValue(json, ListFilterDateRangeValue.class);
    assertEquals(LocalDateTime.of(2026, 1, 2, 0, 0, 0), result.getFrom());
  }

  @Test
  void shouldDeserializeDateOnlyStringForTo() throws Exception {
    final var json =
        """
        {"type":"DATE_RANGE","from":null,"to":"2026-03-15"}
        """;
    final var result = objectMapper.readValue(json, ListFilterDateRangeValue.class);
    assertEquals(LocalDateTime.of(2026, 3, 15, 0, 0, 0), result.getTo());
  }

  @Test
  void shouldDeserializeFullDateTimeStringForFrom() throws Exception {
    final var json =
        """
        {"type":"DATE_RANGE","from":"2026-01-02T10:30:00","to":null}
        """;
    final var result = objectMapper.readValue(json, ListFilterDateRangeValue.class);
    assertEquals(LocalDateTime.of(2026, 1, 2, 10, 30, 0), result.getFrom());
  }

  @Test
  void shouldDeserializeFullDateTimeStringForTo() throws Exception {
    final var json =
        """
        {"type":"DATE_RANGE","from":null,"to":"2026-03-15T23:59:59"}
        """;
    final var result = objectMapper.readValue(json, ListFilterDateRangeValue.class);
    assertEquals(LocalDateTime.of(2026, 3, 15, 23, 59, 59), result.getTo());
  }

  @Test
  void shouldDeserializeNullFromAsNull() throws Exception {
    final var json =
        """
        {"type":"DATE_RANGE","from":null,"to":null}
        """;
    final var result = objectMapper.readValue(json, ListFilterDateRangeValue.class);
    assertNull(result.getFrom());
    assertNull(result.getTo());
  }
}
