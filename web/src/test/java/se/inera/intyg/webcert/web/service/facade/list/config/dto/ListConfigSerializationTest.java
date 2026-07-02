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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import tools.jackson.databind.ObjectMapper;

class ListConfigSerializationTest {

  private final ObjectMapper objectMapper = new CustomObjectMapper();

  @Test
  void shouldSerializeSelectFilterWithTypeProperty() throws Exception {
    final var config =
        new ListConfig(
            List.of(
                new ListFilterSelectConfig(
                    "status",
                    "Status",
                    List.of(ListFilterConfigValue.create("ACTIVE", "Active", true)))),
            "Title",
            "Description",
            "Empty",
            "Secondary",
            List.of(10, 20),
            new TableHeading[] {},
            false);

    final var json = objectMapper.writeValueAsString(config);

    assertTrue(json.contains("\"type\":\"SELECT\""));
    assertTrue(json.contains("\"id\":\"status\""));
  }
}
