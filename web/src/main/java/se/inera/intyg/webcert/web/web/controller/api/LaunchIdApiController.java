/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.inera.intyg.webcert.web.web.controller.api.dto.InvalidateRequest;

@Path(LaunchIdApiController.SESSION_STATUS_REQUEST_MAPPING)
@Api(value = "invalidateSession", description = "API för att ta bort en session relaterad till ett launchId",
    produces = MediaType.APPLICATION_JSON)
public class LaunchIdApiController {

    public static final String SESSION_STATUS_REQUEST_MAPPING = "/v1/session";
    public static final String INVALIDATE_ENDPOINT = "/invalidate";
    protected static final String UTF_8_CHARSET = ";charset=utf-8";

    @POST
    @Path(INVALIDATE_ENDPOINT)
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response invalidateSession(InvalidateRequest invalidateRequest) {
        if (invalidateRequest.formatIsWrong()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }
}
