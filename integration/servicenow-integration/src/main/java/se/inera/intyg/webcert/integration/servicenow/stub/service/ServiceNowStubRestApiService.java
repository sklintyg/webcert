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
package se.inera.intyg.webcert.integration.servicenow.stub.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.servicenow.dto.Organization;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationRequest;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationResponse;
import se.inera.intyg.webcert.integration.servicenow.stub.settings.state.ServiceNowStubState;

@Service
@RequiredArgsConstructor
public class ServiceNowStubRestApiService {

  private final ServiceNowStubState stubState;

  public ResponseEntity<Object> createSubscriptionResponse(
      String basicAuth, OrganizationRequest request) {
    if (basicAuth == null) {
      return ResponseEntity.badRequest().body("Authorization header required by ServiceNow stub.");
    } else if (stubState.getHttpErrorCode() != 0) {
      return responseWithErrorStatusCode(stubState.getHttpErrorCode());
    } else {
      return getSubscriptions(request.getCustomers());
    }
  }

  private ResponseEntity<Object> getSubscriptions(List<String> organizationNumbers) {
    final var activeSubscriptions = stubState.getActiveSubscriptions();
    final var organizations = new ArrayList<Organization>();

    for (var organizationNumber : organizationNumbers) {
      final var subscribedServiceCodes =
          getSubscribedServiceCodes(activeSubscriptions, organizationNumber);
      organizations.add(
          Organization.builder()
              .organizationNumber(organizationNumber)
              .serviceCodes(subscribedServiceCodes)
              .build());
    }

    final var organizationResponse = OrganizationResponse.builder().result(organizations).build();
    return ResponseEntity.ok(organizationResponse);
  }

  private List<String> getSubscribedServiceCodes(
      Map<String, List<String>> activeSubscriptions, String orgNumber) {
    if (!activeSubscriptions.isEmpty() && activeSubscriptions.containsKey(orgNumber)) {
      return activeSubscriptions.get(orgNumber);
    }

    if (stubState.isNotSubscribed(orgNumber)) {
      return Collections.emptyList();
    }

    if (activeSubscriptions.isEmpty() && stubState.getSubscriptionReturnValue()) {
      return stubState.getServiceCodeList();
    }
    return Collections.emptyList();
  }

  private ResponseEntity<Object> responseWithErrorStatusCode(int statusCode) {
    try {
      return ResponseEntity.status(statusCode)
          .body("Http error " + statusCode + " response from ServiceNow stub.");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Http error 500 response from ServiceNow stub.");
    }
  }
}
