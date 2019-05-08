/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;

@Path("/fragasvar")
@Api(value = "fragasvar", description = "REST API för fråga/svar", produces = MediaType.APPLICATION_JSON)
public class FragaSvarApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(FragaSvarApiController.class);

    @Autowired
    private ArendeService arendeService;

    @GET
    @Path("/sok")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response query(@QueryParam("") QueryFragaSvarParameter queryParam) {
        QueryFragaSvarResponse result = arendeService.filterArende(queryParam);
        LOG.debug("/api/fragasvar/sok about to return : " + result.getTotalCount());
        return Response.ok(result).build();
    }

    @GET
    @Path("/lakare")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getFragaSvarLakareByEnhet(@QueryParam("enhetsId") String enhetsId) {
        return Response.ok(arendeService.listSignedByForUnits(enhetsId)).build();
    }
}
