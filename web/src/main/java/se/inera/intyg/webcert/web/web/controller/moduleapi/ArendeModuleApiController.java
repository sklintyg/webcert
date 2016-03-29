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
package se.inera.intyg.webcert.web.web.controller.moduleapi;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeConversationView;

@Path("/arende")
@Api(value = "arende", description = "REST API - moduleapi - arende", produces = MediaType.APPLICATION_JSON)
public class ArendeModuleApiController extends AbstractApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArendeModuleApiController.class);
    @Autowired
    private ArendeService arendeService;

    @GET
    @Path("/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public List<ArendeConversationView> arendeForIntyg(@PathParam("intygsId") String intygsId) {
        List<ArendeConversationView> arenden = arendeService.getArenden(intygsId);

        return arenden;
    }

    @GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_XML)
    public Response getPing() {
        String xmlResponse = buildXMLResponse(true, 0, null);
        LOGGER.debug("Pinged Intygstjänsten, got: " + xmlResponse);
        return Response.ok(xmlResponse).build();
    }

    private String buildXMLResponse(boolean ok, long time, Map<String, String> additionalValues) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pingdom_http_custom_check>");
        sb.append("<status>" + (ok ? "OK" : "FAIL") + "</status>");
        sb.append("<response_time>" + time + "</response_time>");
        if (additionalValues != null) {
            sb.append("<additional_data>");
            additionalValues.forEach((k, v) -> sb.append("<" + k + ">" + v + "</" + k + ">"));
            sb.append("</additional_data>");
        }
        sb.append("</pingdom_http_custom_check>");
        return sb.toString();
    }

}
