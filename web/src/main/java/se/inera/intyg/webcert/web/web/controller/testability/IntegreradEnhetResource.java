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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.web.controller.testability.dto.IntegreradEnhetEntryWithSchemaVersion;

/**
 * Testbarhetsresurs för att till och börja med radera och lista identifierade integrerade
 * vårdenheter.
 */
@RestController
@RequestMapping("/testability/integreradevardenheter")
@Profile({"dev", "testability-api"})
public class IntegreradEnhetResource {

  private static final int OK = 200;
  private static final int BAD_REQUEST = 400;

  @Autowired private IntegreradeEnheterRegistry integreradeEnheterRegistry;

  @DeleteMapping("/{hsaId}")
  public ResponseEntity<String> deleteIntegreradVardenhet(@PathVariable("hsaId") String hsaId) {
    if (isNullOrEmpty(hsaId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Specified hsaId is null or blank");
    }

    integreradeEnheterRegistry.deleteIntegreradEnhet(hsaId);
    return ResponseEntity.ok().build();
  }

  private boolean isNullOrEmpty(String hsaId) {
    return hsaId == null || hsaId.trim().length() == 0;
  }

  @GetMapping
  public ResponseEntity<Object> getIntegreradeVardenheter() {
    return ResponseEntity.ok(integreradeEnheterRegistry.getIntegreradeVardenheter());
  }

  @PostMapping
  public ResponseEntity<IntegreradEnhetEntryWithSchemaVersion> registerIntegreradVardenhet(
      @RequestBody IntegreradEnhetEntryWithSchemaVersion enhet) {
    integreradeEnheterRegistry.putIntegreradEnhet(
        enhet, "1.0".equals(enhet.getSchemaVersion()), "2.0".equals(enhet.getSchemaVersion()));
    return ResponseEntity.ok(enhet);
  }
}
