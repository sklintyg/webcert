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
package se.inera.intyg.webcert.web.web.controller.testability;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.swagger.annotations.Api;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.webcert.integration.fmb.services.FmbService;

@Api(value = "testability fmb", description = "REST API för testbarhet - FMB")
@Path("/fmb")
public class FmbResource {

    @Autowired
    private Optional<FmbService> fmbService;

    /**
     * Populate FMB data using the configured endpoint. Using a GET to update data might
     * not be recommended. However, it is a very convenient way to populate FMB data from
     * the browser without waiting for the automatic population that happens once each
     * day. It is also the only way I could figure out to invoke it from the browser
     * session in the Fitnesse tests.
     */
    @GET
    @Path("/updatefmbdata")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonPropertyDescription("Update FMB data")
    public Response updateFmbData() {
        if (fmbService.isPresent()) {
            fmbService.get().updateData();
            return Response.ok().build();
        } else {
            return Response.serverError().entity("FMB Service not running").build();
        }
    }

}
