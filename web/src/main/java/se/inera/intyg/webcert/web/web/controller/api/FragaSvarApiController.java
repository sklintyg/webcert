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

package se.inera.intyg.webcert.web.web.controller.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import io.swagger.annotations.Api;

@Path("/fragasvar")
@Api(value = "fragasvar", description = "REST API för fråga/svar", produces = MediaType.APPLICATION_JSON)
public class FragaSvarApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(FragaSvarApiController.class);

    @Autowired
    private FragaSvarService fragaSvarService;

    @GET
    @Path("/sok")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response query(@QueryParam("") QueryFragaSvarParameter queryParam) {
        authoritiesValidator.given(getWebCertUserService().getUser()).features(WebcertFeature.HANTERA_FRAGOR).orThrow();
        QueryFragaSvarResponse result = fragaSvarService.filterFragaSvar(queryParam);
        LOG.debug("/api/fragasvar/sok about to return : " + result.getTotalCount());
        return Response.ok(result).build();
    }

    @GET
    @Path("/lakare")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getFragaSvarLakareByEnhet(@QueryParam("enhetsId") String enhetsId) {
        authoritiesValidator.given(getWebCertUserService().getUser()).features(WebcertFeature.HANTERA_FRAGOR).orThrow();
        List<Lakare> lakare = fragaSvarService.getFragaSvarHsaIdByEnhet(enhetsId);
        return Response.ok(lakare).build();
    }
}
