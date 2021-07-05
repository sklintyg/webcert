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

package se.inera.intyg.webcert.integration.kundportalen.stub.api;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.stereotype.Controller;
import se.inera.intyg.webcert.integration.kundportalen.stub.service.KundportalenStubRestApiService;

@Controller
@Path("/api/v1")
public class KundportalenStubRestApi {

    private final KundportalenStubRestApiService kundportalenStubRestApiService;

    public KundportalenStubRestApi(KundportalenStubRestApiService kundportalenStubRestApiService) {
        this.kundportalenStubRestApiService = kundportalenStubRestApiService;
    }

    @POST
    @Path("/service/subscription/Intygstjänster")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSubscriptionPost(@HeaderParam("Authorization") String accessToken, List<String> orgNumbers) {
        return kundportalenStubRestApiService.createSubscriptionResponse(accessToken, orgNumbers);
    }
}
