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
package se.inera.intyg.webcert.web.web.controller.moduleapi;

import io.swagger.annotations.Api;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeConversationView;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.CreateMessageParameter;

@Path("/arende")
@Slf4j
@Api(value = "arende", produces = MediaType.APPLICATION_JSON)
public class ArendeModuleApiController extends AbstractApiController {

    @Autowired
    private ArendeService arendeService;

    @GET
    @Path("/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "arende-module-arende-for-certificate", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public List<ArendeConversationView> arendeForIntyg(@PathParam("intygsId") String intygsId) {
        log.debug("Get arende for intyg {}", intygsId);
        return arendeService.getArenden(intygsId);
    }

    @POST
    @Path("/{intygsTyp}/{intygsId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "arende-module-create-message", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response createMessage(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") final String intygsId,
        CreateMessageParameter parameter) {
        log.debug("Create arende for {} ({})", intygsId, intygsTyp);
        ArendeConversationView response = arendeService.createMessage(intygsId, parameter.getAmne(), parameter.getRubrik(),
            parameter.getMeddelande());
        return Response.ok(response).build();
    }

    @PUT
    @Path("/{intygsTyp}/{meddelandeId}/besvara")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "arende-module-answer", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response answer(@PathParam("intygsTyp") String intygsTyp, @PathParam("meddelandeId") final String meddelandeId,
        String svarsText) {
        log.debug("Answer arende {}", meddelandeId);
        ArendeConversationView response = arendeService.answer(meddelandeId, svarsText);
        return Response.ok(response).build();
    }

    @PUT
    @Path("/{intygsId}/besvara")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "arende-module-answer-complement", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response answer(@PathParam("intygsId") final String intygsId, final String svarsText) {
        log.debug("Answer arenden for intyg {}", intygsId);
        final List<ArendeConversationView> response = arendeService.answerKomplettering(intygsId, svarsText);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{intygsId}/vidarebefordrad")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "arende-module-set-forwarded", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response setForwarded(@PathParam("intygsId") final String intygsId) {
        log.debug("Set arende {} as forwarded true", intygsId);

        List<ArendeConversationView> response = arendeService.setForwarded(intygsId);
        return Response.ok(response).build();
    }

    @PUT
    @Path("/{intygsTyp}/{meddelandeId}/stang")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "arende-module-close-as-handled", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response closeAsHandled(@PathParam("intygsTyp") String intygsTyp, @PathParam("meddelandeId") String meddelandeId) {
        log.debug("Close arende {} as handled", meddelandeId);
        ArendeConversationView response = arendeService.closeArendeAsHandled(meddelandeId, intygsTyp);
        return Response.ok(response).build();
    }

    @PUT
    @Path("/{intygsTyp}/{meddelandeId}/oppna")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "arende-module-open-as-unhandled", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response openAsUnhandled(@PathParam("intygsTyp") String intygsTyp, @PathParam("meddelandeId") String meddelandeId) {
        log.debug("Open arende {} as unhandled", meddelandeId);
        ArendeConversationView response = arendeService.openArendeAsUnhandled(meddelandeId);
        return Response.ok(response).build();
    }

    @GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_XML)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "arende-module-get-ping", eventType = MdcLogConstants.EVENT_TYPE_INFO)
    public Response getPing() {
        String xmlResponse = buildXMLResponse(true, 0, null);
        log.debug("Pinged Intygstjänsten, got: {}", xmlResponse);
        return Response.ok(xmlResponse).build();
    }

    private String buildXMLResponse(boolean ok, long time, Map<String, String> additionalValues) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pingdom_http_custom_check>");
        sb.append("<status>").append(ok ? "OK" : "FAIL").append("</status>");
        sb.append("<response_time>").append(time).append("</response_time>");
        if (additionalValues != null) {
            sb.append("<additional_data>");
            additionalValues.forEach((k, v) -> sb.append("<").append(k).append(">").append(v).append("</").append(k).append(">"));
            sb.append("</additional_data>");
        }
        sb.append("</pingdom_http_custom_check>");
        return sb.toString();
    }
}
