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
package se.inera.intyg.webcert.web.web.controller.testability;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.swagger.annotations.Api;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.integration.fmb.services.FmbService;

@Api(value = "testability fmb", description = "REST API för testbarhet - FMB")
@RestController
@RequestMapping("/testability/fmbtest")
@Profile({"dev", "testability-api"})
public class FmbResource {

  @Autowired private Optional<FmbService> fmbService;

  /**
   * Populate FMB data using the configured endpoint. Using a GET to update data might not be
   * recommended. However, it is a very convenient way to populate FMB data from the browser without
   * waiting for the automatic population that happens once each day. It is also the only way I
   * could figure out to invoke it from the browser session in the Fitnesse tests.
   */
  @GetMapping("/updatefmbdata")
  @JsonPropertyDescription("Update FMB data")
  public ResponseEntity<String> updateFmbData() {
    if (fmbService.isPresent()) {
      fmbService.get().updateData();
      return ResponseEntity.<String>ok().build();
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("FMB Service not running");
    }
  }
}
