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
package se.inera.intyg.webcert.web.web.controller.moduleapi;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeConversationView;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.CreateMessageParameter;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

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
        LOGGER.debug("Get arende for intyg {}", intygsId);
        return arendeService.getArenden(intygsId);
    }

    @POST
    @Path("/{intygsTyp}/{intygsId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response createMessage(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") final String intygsId,
            CreateMessageParameter parameter) {
        LOGGER.debug("Create arende for {} ({})", intygsId, intygsTyp);
        abortIfHanteraFragorNotActive(intygsTyp);
        ArendeConversationView response = arendeService.createMessage(intygsId, parameter.getAmne(), parameter.getRubrik(),
                parameter.getMeddelande());
        return Response.ok(response).build();
    }

    @PUT
    @Path("/{intygsTyp}/{meddelandeId}/besvara")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response answer(@PathParam("intygsTyp") String intygsTyp, @PathParam("meddelandeId") final String meddelandeId,
            String svarsText) {
        LOGGER.debug("Answer arende {}", meddelandeId);
        abortIfHanteraFragorNotActive(intygsTyp);
        ArendeConversationView response = arendeService.answer(meddelandeId, svarsText);
        return Response.ok(response).build();
    }

    @PUT
    @Path("/{intygsTyp}/{meddelandeId}/vidarebefordrad")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response setForwarded(@PathParam("intygsTyp") String intygsTyp, @PathParam("meddelandeId") final String meddelandeId,
            Boolean vidarebefordrad) {
        LOGGER.debug("Set arende {} as forwared {}", meddelandeId, vidarebefordrad != null ? vidarebefordrad : "");

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
                .privilege(AuthoritiesConstants.PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR)
                .orThrow();

        ArendeConversationView response = arendeService.setForwarded(meddelandeId, vidarebefordrad != null ? vidarebefordrad : true);
        return Response.ok(response).build();
    }

    @PUT
    @Path("/{intygsTyp}/{meddelandeId}/stang")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response closeAsHandled(@PathParam("intygsTyp") String intygsTyp, @PathParam("meddelandeId") String meddelandeId) {
        LOGGER.debug("Close arende {} as handled", meddelandeId);
        abortIfHanteraFragorNotActive(intygsTyp);
        ArendeConversationView response = arendeService.closeArendeAsHandled(meddelandeId, intygsTyp);
        return Response.ok(response).build();
    }

    @PUT
    @Path("/{intygsTyp}/{meddelandeId}/oppna")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response openAsUnhandled(@PathParam("intygsTyp") String intygsTyp, @PathParam("meddelandeId") String meddelandeId) {
        LOGGER.debug("Open arende {} as unhandled", meddelandeId);
        abortIfHanteraFragorNotActive(intygsTyp);
        ArendeConversationView response = arendeService.openArendeAsUnhandled(meddelandeId);
        return Response.ok(response).build();
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

    private void abortIfHanteraFragorNotActive(String intygsTyp) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp).features(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
                .orThrow();
    }

}
