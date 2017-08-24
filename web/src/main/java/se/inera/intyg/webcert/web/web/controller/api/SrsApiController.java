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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Utdatafilter;
import se.inera.intyg.infra.integration.srs.model.SrsException;
import se.inera.intyg.infra.integration.srs.model.SrsResponse;
import se.inera.intyg.infra.integration.srs.services.SrsService;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/srs")
@Api(value = "srs", description = "REST API för Stöd för rätt sjukskrivning", produces = MediaType.APPLICATION_JSON)
public class SrsApiController extends AbstractApiController {

    private static final int OK = 200;
    private static final int NO_CONTENT = 204;
    private static final int BAD_REQUEST = 400;

    @Autowired
    private SrsService srsService;

    @GET
    @Path("/{personnummer}/{diagnosisCode}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get SRS data", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = OK, message = "SRS data found", response = SrsResponse.class),
            @ApiResponse(code = BAD_REQUEST, message = "Bad request"),
            @ApiResponse(code = NO_CONTENT, message = "No prediction model found")
    })
    public Response getSrs(@ApiParam(value = "Personnummer", required = true) @PathParam("personnummer") String personnummer,
                           @ApiParam(value = "Diagnosis Code", required = true) @PathParam("diagnosisCode") String diagnosisCode,
                           @ApiParam(value = "Utdatafilter: Prediktion") @QueryParam("isPrediktion") @DefaultValue("false") boolean isPrediktion,
                           @ApiParam(value = "Utdatafilter: AtgardRekommendation") @QueryParam("isAtgard") @DefaultValue("false") boolean isAtgard,
                           @ApiParam(value = "Utdatafilter: FmbInformation") @QueryParam("isFmbInfo") @DefaultValue("false") boolean isFmbInfo,
                           @ApiParam(value = "Utdatafilter: Statistik") @QueryParam("isStatistik") @DefaultValue("false") boolean isStatistik) {
        if (personnummer == null || personnummer.isEmpty() || diagnosisCode == null || diagnosisCode.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Bad request").build();
        }
        try {
            Utdatafilter filter = buildUtdatafilter(isPrediktion, isAtgard, isFmbInfo, isStatistik);
            return Response.ok(srsService.getSrs(new Personnummer(personnummer), diagnosisCode, filter)).build();
        } catch (InvalidPersonNummerException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Faulty personnummer").build();
        } catch (SrsException e) {
            return Response.status(Response.Status.NO_CONTENT).entity("No prediction model found for diagnosis code " + diagnosisCode).build();
        }
    }

    private Utdatafilter buildUtdatafilter(boolean isPrediktion, boolean isAtgard, boolean isFmbInfo, boolean isStatistik) {
        Utdatafilter filter = new Utdatafilter();
        filter.setPrediktion(isPrediktion);
        filter.setAtgardsrekommendation(isAtgard);
        filter.setFmbinformation(isFmbInfo);
        filter.setStatistik(isStatistik);
        return filter;
    }
}
