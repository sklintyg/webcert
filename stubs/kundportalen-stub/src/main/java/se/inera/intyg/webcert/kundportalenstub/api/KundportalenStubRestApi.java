/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.kundportalenstub.api;

import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import se.inera.intyg.webcert.kundportalenstub.service.KundportalenStubRestApiService;

@Controller
@Path("/api")
public class KundportalenStubRestApi {

    @Autowired
    private KundportalenStubRestApiService kundportalenStubRestApiService;

    @GET
    @Path("/customer/getcustomers")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCustomers(@HeaderParam("Authorization") String authorization) {
        return "/api/customer/customers - not implemented";
    }

    @GET
    @Path("/service/getservices")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, String>> getServices(@HeaderParam("Authorization") String authorization) {
        return kundportalenStubRestApiService.getServices();
    }

    @GET
    @Path("/service/getsubscription/{orgNo}/{serviceCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Boolean> getSubscription(@HeaderParam("Authorization") String authorization,
        @PathParam("orgNo") String organizationNumber, @PathParam("serviceCode") String serviceCode) {
        return Map.of("subscriptionActive", kundportalenStubRestApiService.getSubscriptionInfo(organizationNumber, serviceCode));
    }
}
