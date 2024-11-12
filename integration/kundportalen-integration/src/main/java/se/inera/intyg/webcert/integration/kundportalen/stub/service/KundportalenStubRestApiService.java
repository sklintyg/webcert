/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.integration.kundportalen.stub.service;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.kundportalen.stub.state.KundportalenStubState;

@Service
public class KundportalenStubRestApiService {

    private final KundportalenStubState stubState;

    public KundportalenStubRestApiService(KundportalenStubState stubState) {
        this.stubState = stubState;
    }

    public Response createSubscriptionResponse(String accessToken, String service, List<String> orgNumbers) {
        if (accessToken == null) {
            return Response.status(Status.BAD_REQUEST).entity("Authorization header required by Kundportalen stub.").build();
        } else if (service == null) {
            return Response.status(Status.BAD_REQUEST).entity("Query parameter 'service' required by Kundportalen stub.").build();
        } else if (stubState.getHttpErrorCode() != 0) {
            return responseWithErrorStatusCode(stubState.getHttpErrorCode());
        } else {
            return getSubscriptionInfo(orgNumbers);
        }
    }

    private Response getSubscriptionInfo(List<String> orgNumbers) {
        final var activeSubscriptions = stubState.getActiveSubscriptions();
        final var subscriptionInfo = new ArrayList<Map<String, Object>>();

        for (var orgNumber : orgNumbers) {
            final var organization = new HashMap<String, Object>();
            final var subscribedServiceCodes = getSubscribedServiceCodes(activeSubscriptions, orgNumber);

            organization.put("orgNo", orgNumber);
            organization.put("serviceCode", subscribedServiceCodes);
            subscriptionInfo.add(organization);
        }
        return Response.ok(subscriptionInfo).build();
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
                .entity("Http error " + statusCode + " response from Kundportalen stub.").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Http error 500 response from Kundportalen stub.").build();
        }
    }
}
