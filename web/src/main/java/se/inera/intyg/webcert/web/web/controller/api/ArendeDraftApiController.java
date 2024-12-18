/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeDraftEntry;

@Path("/arende/draft")
public class ArendeDraftApiController {

    @Autowired
    private ArendeDraftService arendeDraftService;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "arende-draft-save", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response save(ArendeDraftEntry entry) {
        if (!entry.isValid()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (arendeDraftService.saveDraft(entry.getIntygId(), entry.getQuestionId(), entry.getText(), entry.getAmne())) {
            return Response.ok().build();
        } else {
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("/{intygId}{questionId:(/[^/]+?)?}")
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "arende-draft-delete", eventType = MdcLogConstants.EVENT_TYPE_DELETION)
    public Response delete(@PathParam("intygId") String intygId, @PathParam("questionId") String questionId) {
        String resolvedQuestionId = StringUtils.trimToNull(questionId);
        if (arendeDraftService.delete(intygId, resolvedQuestionId)) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/{intygId}")
    @Produces(MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "arende-draft-get-question-draft", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getQuestionDraft(@PathParam("intygId") String intygId) {
        ArendeDraft questionDraft = arendeDraftService.getQuestionDraft(intygId);
        if (questionDraft != null) {
            return Response.ok(ArendeDraftEntry.fromArendeDraft(questionDraft)).build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }
}
