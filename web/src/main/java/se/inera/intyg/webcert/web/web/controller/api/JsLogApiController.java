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

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.HEIGHT;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.INTYG_ID;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.INTYG_TYPE;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.WIDTH;

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
    @PrometheusTimeMethod
    public Response debug(String message) {
        LOG.debug(message);
        return ok().build();
    }

    @POST
    @Path("/monitoring")
    @Consumes(APPLICATION_JSON)
    @PrometheusTimeMethod
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
        }
        return ok().build();
    }

    @POST
    @Path("/srs")
    @Consumes(APPLICATION_JSON)
    @PrometheusTimeMethod
    public Response srsShown(@RequestBody SrsFrontendEvent event) {
        if (event == null) {
            return status(BAD_REQUEST).build();
        }
        switch (event) {
        case SRS_SHOWN:
            monitoringService.logSrsShown();
            break;
        case SRS_CLICKED:
            monitoringService.logSrsClicked();
            break;
        case SRS_ATGARD_CLICKED:
            monitoringService.logSrsAtgardClicked();
            break;
        case SRS_STATISTIK_CLICKED:
            monitoringService.logSrsStatistikClicked();
            break;
        }
        return ok().build();
    }

    public enum SrsFrontendEvent {
        SRS_SHOWN,
        SRS_CLICKED,
        SRS_ATGARD_CLICKED,
        SRS_STATISTIK_CLICKED
    }
}
