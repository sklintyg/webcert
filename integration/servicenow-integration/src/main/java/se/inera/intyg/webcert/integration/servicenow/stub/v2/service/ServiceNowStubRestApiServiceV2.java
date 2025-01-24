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
package se.inera.intyg.webcert.integration.servicenow.stub.v2.service;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.servicenow.dto.Organization;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationRequestV2;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationResponse;
import se.inera.intyg.webcert.integration.servicenow.stub.settings.state.ServiceNowStubState;

@Service
@RequiredArgsConstructor
public class ServiceNowStubRestApiServiceV2 {

    private final ServiceNowStubState stubState;

    public Response createSubscriptionResponse(String basicAuth, OrganizationRequestV2 request) {
        if (basicAuth == null) {
            return Response.status(Status.BAD_REQUEST).entity("Authorization header required by ServiceNow stub.").build();
        } else if (stubState.getHttpErrorCode() != 0) {
            return responseWithErrorStatusCode(stubState.getHttpErrorCode());
        } else {
            return getSubscriptions(request.getCustomers());
        }
    }

    private Response getSubscriptions(List<String> organizationNumbers) {
        final var activeSubscriptions = stubState.getActiveSubscriptions();
        final var organizations = new ArrayList<Organization>();

        for (var organizationNumber : organizationNumbers) {
            final var subscribedServiceCodes = getSubscribedServiceCodes(activeSubscriptions, organizationNumber);
            organizations.add(Organization.builder()
                .organizationNumber(organizationNumber)
                .serviceCodes(subscribedServiceCodes)
                .build()
            );
        }

        final var organizationResponse = OrganizationResponse.builder()
            .result(organizations).build();

        return Response.ok(organizationResponse).build();
    }

    private List<String> getSubscribedServiceCodes(Map<String, List<String>> activeSubscriptions, String orgNumber) {
        if (!activeSubscriptions.isEmpty() && activeSubscriptions.containsKey(orgNumber)) {
            return activeSubscriptions.get(orgNumber);
        }
        if (activeSubscriptions.isEmpty() && stubState.getSubscriptionReturnValue()) {
            return stubState.getServiceCodeList();
        }
        return Collections.emptyList();
    }

    private Response responseWithErrorStatusCode(int statusCode) {
        try {
            return Response.status(Status.fromStatusCode(statusCode))
                .entity("Http error " + statusCode + " response from ServiceNow stub.").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Http error 500 response from ServiceNow stub.").build();
        }
    }
}
