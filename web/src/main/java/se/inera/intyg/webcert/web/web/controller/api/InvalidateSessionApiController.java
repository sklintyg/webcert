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

import io.swagger.annotations.Api;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.webcert.web.service.launchid.InvalidateSessionService;
import se.inera.intyg.webcert.web.web.controller.api.dto.InvalidateRequest;

@Path(InvalidateSessionApiController.SESSION_STATUS_REQUEST_MAPPING)
@Api(value = "invalidateSession",
    produces = MediaType.APPLICATION_JSON)
public class InvalidateSessionApiController {

    private static final Logger LOG = LoggerFactory.getLogger(InvalidateSessionApiController.class);
    public static final String SESSION_STATUS_REQUEST_MAPPING = "/v1/session";
    public static final String INVALIDATE_ENDPOINT = "/invalidate";
    protected static final String UTF_8_CHARSET = ";charset=utf-8";
    @Autowired
    private InvalidateSessionService invalidateSessionService;

    @POST
    @Path(INVALIDATE_ENDPOINT)
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response invalidateSession(InvalidateRequest invalidateRequest) {
        if (invalidateRequest.formatIsWrong()) {
            LOG.info(String.format("launchId: %s OR userHsaId: %s - is wrong format. request will not be handled any further",
                invalidateRequest.getLaunchId(), invalidateRequest.getUserHsaId()));
            return Response.noContent().build();
        }
        try {
            invalidateSessionService.invalidateSessionIfActive(invalidateRequest);
        } catch (Exception exception) {
            LOG.error("Invalidate session failed. launchId: %s - userHsaId: %s", exception);
        }
        return Response.noContent().build();
    }
}
