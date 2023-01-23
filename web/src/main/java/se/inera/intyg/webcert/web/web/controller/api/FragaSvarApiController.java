/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/fragasvar")
@Api(value = "fragasvar", description = "REST API för fråga/svar", produces = MediaType.APPLICATION_JSON)
public class FragaSvarApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(FragaSvarApiController.class);

    @Autowired
    private ArendeService arendeService;

    @Autowired
    private ResourceLinkHelper resourceLinkHelper;

    @Autowired
    private WebCertUserService webCertUserService;

    @GET
    @Path("/sok")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response query(@QueryParam("") QueryFragaSvarParameter queryParam) {
        QueryFragaSvarResponse result = arendeService.filterArende(queryParam);
        resourceLinkHelper.decorateArendeWithValidActionLinks(result.getResults(), getVardenhet());
        LOG.debug("/api/fragasvar/sok about to return : " + result.getTotalCount());
        return Response.ok(result).build();
    }

    @GET
    @Path("/lakare")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getFragaSvarLakareByEnhet(@QueryParam("enhetsId") String enhetsId) {
        return Response.ok(arendeService.listSignedByForUnits(enhetsId)).build();
    }

    // Get the user´s logged in Vardenhet
    private Vardenhet getVardenhet() {
        final WebCertUser user = webCertUserService.getUser();
        final SelectableVardenhet selectedVardenhet = user.getValdVardenhet();
        final SelectableVardenhet selectedVardgivare = user.getValdVardgivare();

        final Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarid(selectedVardgivare.getId());

        final Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(selectedVardenhet.getId());
        vardenhet.setVardgivare(vardgivare);

        return vardenhet;
    }
}
