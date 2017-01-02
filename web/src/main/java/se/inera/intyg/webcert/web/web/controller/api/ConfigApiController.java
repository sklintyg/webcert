/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.ConfigResponse;

@Path("/config")
@Api(value = "config", description = "REST API för konfigurationsparametrar", produces = MediaType.APPLICATION_JSON)
public class ConfigApiController extends AbstractApiController {

    @Value("${buildNumber}")
    private String build;

    @Value("${privatepractitioner.portal.registration.url}")
    private String ppHost;

    @Value("${certificate.view.url.base}")
    private String dashboardUrl;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get module configuration for Webcert", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    public Response getConfig() {
        return Response.ok(new ConfigResponse(build, ppHost, dashboardUrl)).build();
    }

    @PostConstruct
    public void init() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }
}
