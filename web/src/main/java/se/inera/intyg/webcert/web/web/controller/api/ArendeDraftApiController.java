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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeDraftEntry;

@Path("/arende/draft")
public class ArendeDraftApiController {

    @Autowired
    private ArendeDraftService arendeDraftService;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
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
    public Response getQuestionDraft(@PathParam("intygId") String intygId) {
        ArendeDraft questionDraft = arendeDraftService.getQuestionDraft(intygId);
        if (questionDraft != null) {
            return Response.ok(ArendeDraftEntry.fromArendeDraft(questionDraft)).build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }
}
