/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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


import static java.util.Objects.isNull;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.httpclient.HttpStatus;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.fmb.icf.IcfService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.Icd10KoderRequest;

@Path("/icf")
@Api(value = "fmb", description = "REST API för Försäkringsmedicinskt beslutsstöd", produces = MediaType.APPLICATION_JSON)
public class IcfApiController extends AbstractApiController {

    private IcfService service;

    public IcfApiController(final IcfService service) {
        this.service = service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(
            value = "Get ICF data for ICD10 codes", httpMethod = "GET",
            produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = "Given ICF data for ICD10 codes found", response = FmbResponse.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Bad request due to missing icd10 codes")})
    @PrometheusTimeMethod
    public Response getIcfForIcd10(
            @ApiParam(value = "ICD10 codes", required = true)
            @QueryParam("icd10Kod1") final String icd10Kod1,
            @QueryParam("icd10Kod2") final String icd10Kod2,
            @QueryParam("icd10Kod3") final String icd10Kod3) {

        if (isNull(icd10Kod1)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing icd10 codes").build();
        }

        return Response.ok(service.findIcfInformationByIcd10Koder(Icd10KoderRequest.of(icd10Kod1, icd10Kod2, icd10Kod3))).build();
    }
}
