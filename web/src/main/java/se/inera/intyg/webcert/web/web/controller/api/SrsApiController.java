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

import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.Samtyckesstatus;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Utdatafilter;
import se.inera.intyg.infra.integration.srs.model.SjukskrivningsGrad;
import se.inera.intyg.infra.integration.srs.model.SrsException;
import se.inera.intyg.infra.integration.srs.model.SrsQuestion;
import se.inera.intyg.infra.integration.srs.model.SrsQuestionResponse;
import se.inera.intyg.infra.integration.srs.model.SrsResponse;
import se.inera.intyg.infra.integration.srs.services.SrsService;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

//CHECKSTYLE:OFF ParameterNumber
@Path("/srs")
@Api(value = "srs", description = "REST API för Stöd för rätt sjukskrivning", produces = MediaType.APPLICATION_JSON)
public class SrsApiController extends AbstractApiController {

    private static final int OK = 200;
    private static final int NO_CONTENT = 204;
    private static final int BAD_REQUEST = 400;

    @Autowired
    private SrsService srsService;

    @Autowired
    private LogService logService;

    @POST
    @Path("/{intygId}/{personnummer}/{diagnosisCode}/{sjukskrivningsgrad}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get SRS data", httpMethod = "POST", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = OK, message = "SRS data found", response = SrsResponse.class),
            @ApiResponse(code = BAD_REQUEST, message = "Bad request"),
            @ApiResponse(code = NO_CONTENT, message = "No prediction model found")
    })
    public Response getSrs(@ApiParam(value = "Intyg id", required = true) @PathParam("intygId") String intygId,
            @ApiParam(value = "Personnummer", required = true) @PathParam("personnummer") String personnummer,
            @ApiParam(value = "Diagnosis Code", required = true) @PathParam("diagnosisCode") String diagnosisCode,
            @ApiParam(value = "Sjukskrivningsgrad", required = true) @PathParam("sjukskrivningsgrad") String sjukskrivningsgrad,
            @ApiParam(value = "Utdatafilter: Prediktion") @QueryParam("prediktion") @DefaultValue("false") boolean prediktion,
            @ApiParam(value = "Utdatafilter: AtgardRekommendation") @QueryParam("atgard") @DefaultValue("false") boolean atgard,
            @ApiParam(value = "Utdatafilter: FmbInformation") @QueryParam("fmbInfo") @DefaultValue("false") boolean fmbInfo,
            @ApiParam(value = "Utdatafilter: Statistik") @QueryParam("statistik") @DefaultValue("false") boolean statistik,
            @ApiParam(value = "Svar på frågor") List<SrsQuestionResponse> questions) {
        authoritiesValidator.given(getWebCertUserService().getUser())
                .features(WebcertFeature.SRS)
                .orThrow();

        if (Strings.isNullOrEmpty(personnummer) || Strings.isNullOrEmpty(diagnosisCode)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            Utdatafilter filter = buildUtdatafilter(prediktion, atgard, fmbInfo, statistik);
            SrsResponse response = srsService.getSrs(intygId, new Personnummer(personnummer), diagnosisCode, filter, questions,
                    SjukskrivningsGrad.valueOf(sjukskrivningsgrad));
            logService.logShowPrediction(personnummer);
            return Response.ok(response).build();
        } catch (InvalidPersonNummerException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (SrsException e) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    @GET
    @Path("/questions/{diagnosisCode}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get questions for diagnosis code", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    public Response getQuestions(@ApiParam(value = "Diagnosis code") @PathParam("diagnosisCode") String diagnosisCode) {
        authoritiesValidator.given(getWebCertUserService().getUser())
                .features(WebcertFeature.SRS)
                .orThrow();

        if (Strings.isNullOrEmpty(diagnosisCode)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        List<SrsQuestion> response = srsService.getQuestions(diagnosisCode);
        return Response.ok(response).build();
    }

    @GET
    @Path("/consent/{personnummer}/{hsaId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get consent for patient and careunit", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    public Response getConsent(
            @ApiParam(value = "Personnummer") @PathParam("personnummer") String personnummer,
            @ApiParam(value = "HsaId för vårdenhet") @PathParam("hsaId") String hsaId) {
        authoritiesValidator.given(getWebCertUserService().getUser())
                .features(WebcertFeature.SRS)
                .orThrow();

        try {
            Personnummer p = new Personnummer(personnummer);
            Samtyckesstatus response = srsService.getConsent(hsaId, p);
            return Response.ok(response).build();
        } catch (InvalidPersonNummerException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @PUT
    @Path("/consent/{personnummer}/{hsaId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Set consent for patient and careunit", httpMethod = "PUT", produces = MediaType.APPLICATION_JSON)
    public Response setConsent(
            @ApiParam(value = "Personnummer") @PathParam("personnummer") String personnummer,
            @ApiParam(value = "HsaId för vårdenhet") @PathParam("hsaId") String hsaId,
            boolean consent) {
        authoritiesValidator.given(getWebCertUserService().getUser())
                .features(WebcertFeature.SRS)
                .orThrow();

        try {
            Personnummer p = new Personnummer(personnummer);
            ResultCodeEnum result = srsService.setConsent(hsaId, p, consent);
            return Response.ok(result).build();
        } catch (InvalidPersonNummerException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    private Utdatafilter buildUtdatafilter(boolean prediktion, boolean atgard, boolean fmbInfo, boolean statistik) {
        Utdatafilter filter = new Utdatafilter();
        filter.setPrediktion(prediktion);
        filter.setAtgardsrekommendation(atgard);
        filter.setFmbinformation(fmbInfo);
        filter.setStatistik(statistik);
        return filter;
    }
}
//CHECKSTYLE:ON ParameterNumber
