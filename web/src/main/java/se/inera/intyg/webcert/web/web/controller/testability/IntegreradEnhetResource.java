/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.web.controller.testability.dto.IntegreradEnhetEntryWithSchemaVersion;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Testbarhetsresurs för att till och börja med radera och lista identifierade integrerade vårdenheter.
 */
@Service
@Api(value = "testability integreradevardenheter", description = "REST API för testbarhet - Integrerade vårdenheter")
@Path("/integreradevardenheter")
public class IntegreradEnhetResource {

    private static final int OK = 200;
    private static final int BAD_REQUEST = 400;

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @DELETE
    @Path("/{hsaId}")
    @ApiResponses(value = {
            @ApiResponse(code = OK, message = "Given identified integrated unit was deleted"),
            @ApiResponse(code = BAD_REQUEST, message = "If supplied hsaId was null or blank")
    })
    public Response deleteIntegreradVardenhet(@PathParam("hsaId") String hsaId) {
        if (isNullOrEmpty(hsaId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Specified hsaId is null or blank").build();
        }

        integreradeEnheterRegistry.deleteIntegreradEnhet(hsaId);
        return Response.ok().build();
    }

    private boolean isNullOrEmpty(String hsaId) {
        return hsaId == null || hsaId.trim().length() == 0;
    }

    @GET
    @Path("/")
    @Produces("application/json")
    @ApiResponses(value = {
            @ApiResponse(code = OK, message = "Listed current set of integrerade Vårdenheter")
    })
    public Response getIntegreradeVardenheter() {
        return Response.ok(integreradeEnheterRegistry.getIntegreradeVardenheter()).build();
    }

    @POST
    @Path("/")
    @Consumes("application/json")
    @Produces("application/json")
    @ApiResponses(value = {
            @ApiResponse(code = OK, message = "Registered integrerad vardenhet")
    })
    public Response registerIntegreradVardenhet(IntegreradEnhetEntryWithSchemaVersion enhet) {
        integreradeEnheterRegistry.putIntegreradEnhet(enhet, "1.0".equals(enhet.getSchemaVersion()),
                "2.0".equals(enhet.getSchemaVersion()));
        return Response.ok(enhet).build();
    }

}
