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
package se.inera.intyg.webcert.web.web.controller.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.service.receiver.CertificateReceiverService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygReceiver;

@Path("/receiver")
@Api(value = "receiver", description = "REST API för hantering av intygsmottagare", produces = MediaType.APPLICATION_JSON)
public class ReceiverApiController extends AbstractApiController {

    @Autowired
    private CertificateReceiverService certificateReceiverService;

    /**
     * Used after the signing of the intyg if the doctor wants to see (and possibly edit) approved receivers.
     *
     * @param intygsId
     * @return
     */
    @GET
    @Path("/approvedreceivers/{intygsTyp}/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response listApprovedReceivers(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId) {
        List<IntygReceiver> intygReceivers = certificateReceiverService.listPossibleReceiversWithApprovedInfo(intygsTyp, intygsId);
        return Response.ok(intygReceivers).build();
    }

    /**
     * Lists possible receivers for the intygstyp.
     *
     * @param intygsTyp
     * @return
     */
    @GET
    @Path("/possiblereceivers/{intygsTyp}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response listApprovedReceivers(@PathParam("intygsTyp") String intygsTyp) {
        List<IntygReceiver> intygReceivers = certificateReceiverService.listPossibleReceivers(intygsTyp);
        return Response.ok(intygReceivers).build();
    }

    @POST
    @Path("/registerapproved/{intygsTyp}/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response registerApprovedReceivers(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            List<String> receiverIds) {
        authoritiesValidator
                .given(getWebCertUserService().getUser(), intygsTyp)
                .privilege(AuthoritiesConstants.PRIVILEGE_GODKANNA_MOTTAGARE)
                .orThrow();
        certificateReceiverService.registerApprovedReceivers(intygsId, intygsTyp, receiverIds);
        return Response.ok().build();
    }
}
