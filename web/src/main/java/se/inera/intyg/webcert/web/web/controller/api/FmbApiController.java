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
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.fmb.FmbDiagnosInformationService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.Icd10KoderRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.MaximalSjukskrivningstidRequest;

@Path("/fmb")
@Api(value = "fmb", description = "REST API för Försäkringsmedicinskt beslutsstöd", produces = MediaType.APPLICATION_JSON)
public class FmbApiController extends AbstractApiController {

    private static final int MIN_ICD10_POSITION = 3;

    private static final Logger LOG = LoggerFactory.getLogger(FmbApiController.class);

    private static final int OK = 200;
    private static final int BAD_REQUEST = 400;

    @Autowired
    private FmbDiagnosInformationService fmbDiagnosInformationService;

    // CHECKSTYLE:OFF LineLength
    @GET
    @Path("/{icd10}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get FMB data for ICD10 codes", httpMethod = "GET", notes = "Fetch the admin user details", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = OK, message = "Given FMB data for icd10 code found", response = FmbResponse.class),
            @ApiResponse(code = BAD_REQUEST, message = "Bad request due to missing icd10 code")
    })
    @PrometheusTimeMethod
    public Response getFmbForIcd10(@ApiParam(value = "ICD10 code", required = true) @PathParam("icd10") String icd10) {
        if (icd10 == null || icd10.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing icd10 code").build();
        }

        return fmbDiagnosInformationService.findFmbDiagnosInformationByIcd10Kod(icd10)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElseGet(() -> Response.noContent().build());
    }

    @GET
    @Path("/valideraSjukskrivningstid")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(
            value = "validate sjukskrivningstid for patient and ICD10 codes", httpMethod = "GET",
            produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = "Response Object containing info regardning sjukskrivning for patient", response = FmbResponse.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Bad request due to missing icd10 codes, or missing foreslagenSjukskrivningstid")})
    @PrometheusTimeMethod
    public Response valideraSjukskrivningstid(
            @ApiParam(value = "ICD10 code", required = true) @QueryParam("icd10Kod1") final String icd10Kod1,
            @QueryParam("icd10Kod2") final String icd10Kod2,
            @QueryParam("icd10Kod3") final String icd10Kod3,
            @ApiParam(value = "Föreslagen Sjukskrivningstid", required = true) @QueryParam("foreslagenSjukskrivningstid") final Integer foreslagenSjukskrivningstid,
            @ApiParam(value = "Personnummer för patient", required = true) @QueryParam("personnummer") final String personnummer) {

        List<String> validationErrors = Lists.newArrayList();

        if (isNull(icd10Kod1)) {
            validationErrors.add("Missing icd10 codes");
        }

        if (isNull(foreslagenSjukskrivningstid)) {
            validationErrors.add("Missing foreslagenSjukskrivningstid");
        }

        if (isNull(personnummer)) {
            validationErrors.add("Missing personnummer");
        }

        final Optional<Personnummer> optionalPersonnummer = Personnummer.createPersonnummer(personnummer);
        if (!optionalPersonnummer.isPresent()) {
            validationErrors.add("Incorrect personnummer format");
        }

        if (isNotEmpty(validationErrors)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(String.join(",", validationErrors)).build();
        }

        return Response.ok(fmbDiagnosInformationService.validateSjukskrivningtidForPatient(
                MaximalSjukskrivningstidRequest.of(
                        Icd10KoderRequest.of(icd10Kod1, icd10Kod2, icd10Kod3),
                        optionalPersonnummer.get(),
                        foreslagenSjukskrivningstid)))
                .build();
    }
    // CHECKSTYLE:ON LineLength
}
