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

package se.inera.intyg.webcert.web.web.controller.monitoring;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.web.service.monitoring.HealthCheckService;
import se.inera.intyg.webcert.web.service.monitoring.dto.HealthStatus;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;

/**
 * RESTinterface for checking the general health status of the application.
 *
 * @author nikpet
 *
 */
@Api(value = "monitoring", description = "REST API för monitorering/healthcheck", produces = MediaType.APPLICATION_XML)
public class HealthCheckApiController extends AbstractApiController {

    @Autowired
    private HealthCheckService healthCheck;

    @GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_XML)
    public Response checkPing() {
        String xmlResponse = buildXMLResponse(true, 0);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/db")
    @Produces(MediaType.APPLICATION_XML)
    public Response checkDB() {
        HealthStatus status = healthCheck.checkDB();
        String xmlResponse = buildXMLResponse(status);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/jms")
    @Produces(MediaType.APPLICATION_XML)
    public Response checkJMS() {
        HealthStatus status = healthCheck.checkJMS();
        String xmlResponse = buildXMLResponse(status);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/hsa-authorizationmanagement")
    @Produces(MediaType.APPLICATION_XML)
    public Response checkHSA() {
        HealthStatus status = healthCheck.checkHsaAuthorizationmanagement();
        String xmlResponse = buildXMLResponse(status);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/hsa-employee")
    @Produces(MediaType.APPLICATION_XML)
    public Response checkHsaEmployee() {
        HealthStatus status = healthCheck.checkHsaEmployee();
        String xmlResponse = buildXMLResponse(status);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/hsa-organization")
    @Produces(MediaType.APPLICATION_XML)
    public Response checkHsaOrganization() {
        HealthStatus status = healthCheck.checkHsaOrganization();
        String xmlResponse = buildXMLResponse(status);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/signature-queue")
    @Produces(MediaType.APPLICATION_XML)
    public Response checkSignatureQueue() {
        HealthStatus status = healthCheck.checkSignatureQueue();
        String xmlResponse = buildXMLResponse(status);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/intygstjanst")
    @Produces(MediaType.APPLICATION_XML)
    public Response checkIntygstjanst() {
        HealthStatus status = healthCheck.checkIntygstjanst();
        String xmlResponse = buildXMLResponse(status);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/uptime")
    @Produces(MediaType.APPLICATION_XML)
    public Response checkUptime() {
        HealthStatus status = healthCheck.checkUptime();
        String xmlResponse = buildXMLResponse(status);
        return Response.ok(xmlResponse).build();
    }

    private String buildXMLResponse(HealthStatus status) {
        return buildXMLResponse(status.isOk(), status.getMeasurement());
    }

    private String buildXMLResponse(boolean ok, long time) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pingdom_http_custom_check>");
        sb.append("<status>" + (ok ? "OK" : "FAIL") + "</status>");
        sb.append("<response_time>" + time + "</response_time>");
        sb.append("</pingdom_http_custom_check>");
        return sb.toString();
    }
}
