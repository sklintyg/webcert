/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.HEIGHT;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.INTYG_ID;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.INTYG_TYPE;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.WIDTH;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest;

/**
 * Controller that logs messages from JavaScript to the normal log.
 */
@Path("/jslog")
@Api(value = "jslog", description = "REST API för loggning från frontend till backend-log", produces = MediaType.APPLICATION_JSON)
public class JsLogApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(JsLogApiController.class);

    @Autowired
    private MonitoringLogService monitoringService;

    @POST
    @Path("/debug")
    public Response debug(String message) {
        LOG.debug(message);
        return ok().build();
    }

    @POST
    @Path("/monitoring")
    @Consumes(APPLICATION_JSON)
    public Response monitoring(MonitoringRequest request) {
        if (request == null || !request.isValid()) {
            return status(BAD_REQUEST).build();
        }

        switch (request.getEvent()) {
        case SCREEN_RESOLUTION:
            monitoringService.logScreenResolution(request.getInfo().get(WIDTH), request.getInfo().get(HEIGHT));
            break;
        case DIAGNOSKODVERK_CHANGED:
            monitoringService.logDiagnoskodverkChanged(request.getInfo().get(INTYG_ID), request.getInfo().get(INTYG_TYPE));
            break;
        default:
            return status(Status.BAD_REQUEST).build();
        }
        return ok().build();
    }
}
