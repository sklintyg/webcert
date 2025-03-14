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
package se.inera.intyg.webcert.integration.servicenow.stub.v2.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationRequestV2;
import se.inera.intyg.webcert.integration.servicenow.stub.v2.service.ServiceNowStubRestApiServiceV2;

@Controller
@RequiredArgsConstructor
@Path("/api/nabia/v2/inera_services/services")
public class ServiceNowStubRestApiV2 {

    private final ServiceNowStubRestApiServiceV2 serviceNowStubRestApiServiceV2;

    @POST
    @Path("/stub")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSubscriptionPost(@HeaderParam("Authorization") String basicAuth, OrganizationRequestV2 request) {
        return serviceNowStubRestApiServiceV2.createSubscriptionResponse(basicAuth, request);
    }
}
