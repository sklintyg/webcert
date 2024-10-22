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
package se.inera.intyg.webcert.web.web.controller.testability;

import io.swagger.annotations.Api;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.anvandarmetadata.model.AnvandarPreference;
import se.inera.intyg.webcert.persistence.anvandarmetadata.repository.AnvandarPreferenceRepository;
import se.inera.intyg.webcert.persistence.privatlakaravtal.repository.AvtalRepository;
import se.inera.intyg.webcert.persistence.privatlakaravtal.repository.GodkantAvtalRepository;

@Transactional
@Api(value = "services anvandare", description = "REST API för testbarhet - Användare")
@Path("/anvandare")
public class UserAgreementResource {

    @Autowired
    private AvtalRepository avtalRepository;

    @Autowired
    private GodkantAvtalRepository godkantAvtalRepository;

    @Autowired
    private AnvandarPreferenceRepository anvandarPreferenceRepository;

    @PUT
    @Path("/godkannavtal/{hsaId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response godkannAvtal(@PathParam("hsaId") String hsaId) {
        int avtalVersion = avtalRepository.getLatestAvtalVersion();
        godkantAvtalRepository.approveAvtal(hsaId, avtalVersion);
        return Response.ok().build();
    }

    @PUT
    @Path("/avgodkannavtal/{hsaId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response avgodkannAvtal(@PathParam("hsaId") String hsaId) {
        godkantAvtalRepository.removeAllUserApprovments(hsaId);
        return Response.ok().build();
    }

    @GET
    @Path("/approvedTerms/{hsaId}")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean getTermsApproval(@PathParam("hsaId") String hsaId) {
        return godkantAvtalRepository.userHasApprovedAvtal(hsaId, avtalRepository.getLatestAvtalVersion());
    }

    @DELETE
    @Path("/preferences/{hsaId}/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePreference(@PathParam("hsaId") String hsaId, @PathParam("key") String key) {
        AnvandarPreference ap = anvandarPreferenceRepository.findByHsaIdAndKey(hsaId, key);
        if (ap != null) {
            anvandarPreferenceRepository.delete(ap);
        }
        return Response.ok().build();
    }

}
