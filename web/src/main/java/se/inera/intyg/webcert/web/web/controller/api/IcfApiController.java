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


import static java.util.Objects.isNull;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.httpclient.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.fmb.FmbDiagnosInformationServiceImpl;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbResponse;

@Path("/icf")
@Api(value = "fmb", description = "REST API för Försäkringsmedicinskt beslutsstöd", produces = MediaType.APPLICATION_JSON)
public class IcfApiController extends AbstractApiController {

    private FmbDiagnosInformationServiceImpl service;

    public IcfApiController(final FmbDiagnosInformationServiceImpl service) {
        this.service = service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(
            value = "Get FMB data for ICD10 codes", httpMethod = "GET",
            notes = "Fetch the admin user details", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = "Given ICF data for icd10 code found", response = FmbResponse.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Bad request due to missing icd10 codes")})
    @PrometheusTimeMethod
    public Response getIcfForIcd10(
            @ApiParam(value = "ICD10 codes", required = true)
            @RequestBody final IcfRequest request) {

        if (isNull(request) || isNull(request.getIcd10Code1())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing icd10 codes").build();
        }

        service.findIcfInformationByIcd10Koder(request);

        return Response.ok().build();
    }
}
