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
package se.inera.intyg.webcert.integration.servicenow.stub.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationRequest;
import se.inera.intyg.webcert.integration.servicenow.stub.service.ServiceNowStubRestApiService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stubs/servicenowstub/api/nabia/v2/inera_services/services")
public class ServiceNowStubRestApi {

  private final ServiceNowStubRestApiService serviceNowStubRestApiService;

  @PostMapping(
      value = "/stub",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getSubscriptionPost(
      @RequestHeader("Authorization") String basicAuth, @RequestBody OrganizationRequest request) {
    return serviceNowStubRestApiService.createSubscriptionResponse(basicAuth, request);
  }
}
