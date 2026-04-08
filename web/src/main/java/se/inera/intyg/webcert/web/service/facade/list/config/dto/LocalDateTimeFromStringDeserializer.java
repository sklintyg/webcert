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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Deserializes a JSON string into {@link LocalDateTime}, accepting both:
 *
 * <ul>
 *   <li>Date-only format {@code yyyy-MM-dd} — interpreted as the start of that day ({@code
 *       T00:00:00})
 *   <li>Full datetime format {@code yyyy-MM-dd'T'HH:mm:ss[.nnnnnnnnn]}
 * </ul>
 */
public class LocalDateTimeFromStringDeserializer extends StdDeserializer<LocalDateTime> {

  public LocalDateTimeFromStringDeserializer() {
    super(LocalDateTime.class);
  }

  @Override
  public LocalDateTime deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {
    final String value = parser.getText().trim();
    if (value.isEmpty()) {
      return null;
    }
    // Date-only string: "yyyy-MM-dd"
    if (value.length() == 10 && !value.contains("T")) {
      return LocalDate.parse(value).atStartOfDay();
    }
    return LocalDateTime.parse(value);
  }
}
