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
package se.inera.intyg.webcert.web.web.controller.api;

import io.swagger.annotations.Api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.privatepractitioner.PrivatePractitionerService;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.HospInformationResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerConfigResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerDetails;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerResponse;

@RestController
@RequestMapping("/api/private-practitioner")
@Api(value = "private-practitioner", produces = "application/json")
public class PrivatePractitionerApiController {

  private static final String UTF_8_CHARSET = ";charset=utf-8";

  private PrivatePractitionerService service;

  public PrivatePractitionerApiController(PrivatePractitionerService service) {
    this.service = service;
  }

  @PostMapping
  @PerformanceLogging(
      eventAction = "register-private-practitioner",
      eventType = MdcLogConstants.EVENT_TYPE_CREATION)
  public ResponseEntity<Void> registerPractitioner(
      @RequestBody PrivatePractitionerDetails registerPrivatePractitionerRequest) {
    service.registerPrivatePractitioner(registerPrivatePractitionerRequest);
    return ResponseEntity.ok().build();
  }

  @GetMapping
  @PerformanceLogging(
      eventAction = "get-private-practitioner",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public PrivatePractitionerResponse getPrivatePractitioner() {
    return service.getLoggedInPrivatePractitioner();
  }

  @GetMapping("/config")
  @PerformanceLogging(
      eventAction = "get-private-practitioner-config",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public PrivatePractitionerConfigResponse getPrivatePractitionerConfig() {
    return service.getPrivatePractitionerConfig();
  }

  @GetMapping("/hospInformation")
  @PerformanceLogging(
      eventAction = "get-private-practitioner-hosp-information",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public HospInformationResponse getHospInformation() {
    return service.getHospInformation();
  }

  @PutMapping
  @PerformanceLogging(
      eventAction = "update-private-practitioner",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public PrivatePractitionerResponse updatePrivatePractitioner(
      @RequestBody PrivatePractitionerDetails updatePrivatePractitionerRequest) {
    return service.editPrivatePractitioner(updatePrivatePractitionerRequest);
  }
}
