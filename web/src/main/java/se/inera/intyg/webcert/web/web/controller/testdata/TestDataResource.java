/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.testdata;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.webcert.web.service.testdata.TestDataService;

@Path("/testdata")
@Api(value = "testdata", description = "REST API för inskjutning av testdata", produces = MediaType.APPLICATION_JSON)
public class TestDataResource {

    private static final Logger LOG = LoggerFactory.getLogger(TestDataResource.class);
    protected static final String UTF_8_CHARSET = ";charset=utf-8";

    @Autowired
    private TestDataService service;

    @GET
    @Path("/")
    public Response intyg() {
        return Response.ok().build();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertIntyg(IntygWrapper data) {

        service.createIntyg(data.getData());

        return Response.ok().build();
    }

    @DELETE
    @Path("/")
    public Response deleteIntyg() {

        service.deleteIntyg();

        return Response.ok().build();
    }

    static class IntygWrapper {

        JsonNode data;

        JsonNode getData() {
            return data;
        }

        void setData(JsonNode data) {
            this.data = data;
        }
    }
}
