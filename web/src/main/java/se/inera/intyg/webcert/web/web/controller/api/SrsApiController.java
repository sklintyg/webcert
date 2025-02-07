/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.Samtyckesstatus;
import se.inera.intyg.infra.integration.srs.model.SrsForDiagnosisResponse;
import se.inera.intyg.infra.integration.srs.model.SrsQuestion;
import se.inera.intyg.infra.integration.srs.model.SrsQuestionResponse;
import se.inera.intyg.infra.integration.srs.model.SrsResponse;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.srs.SrsService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum;

//CHECKSTYLE:OFF ParameterNumber
@Path("/srs")
@Api(value = "srs", description = "REST API för Stöd för rätt sjukskrivning", produces = MediaType.APPLICATION_JSON)
public class SrsApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(SrsApiController.class);

    private static final int OK = 200;
    private static final int NO_CONTENT = 204;
    private static final int BAD_REQUEST = 400;

    @Autowired
    private SrsService srsService;

    @POST
    @Path("/{intygId}/{personnummer}/{diagnosisCode}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get SRS data", httpMethod = "POST", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(code = OK, message = "SRS data found", response = SrsResponse.class),
        @ApiResponse(code = BAD_REQUEST, message = "Bad request"),
        @ApiResponse(code = NO_CONTENT, message = "No prediction model found")
    })
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "srs-get-srs", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getSrs(@ApiParam(value = "Intyg id", required = true) @PathParam("intygId") String intygId,
        @ApiParam(value = "Personnummer", required = true) @PathParam("personnummer") String personnummer,
        @ApiParam(value = "Diagnosis Code", required = true) @PathParam("diagnosisCode") String diagnosisCode,
        @ApiParam(value = "Utdatafilter: Prediktion") @QueryParam("prediktion") @DefaultValue("false") boolean prediktion,
        @ApiParam(value = "Utdatafilter: AtgardRekommendation") @QueryParam("atgard") @DefaultValue("false") boolean atgard,
        @ApiParam(value = "Utdatafilter: Statistik") @QueryParam("statistik") @DefaultValue("false") boolean statistik,
        @ApiParam(value = "Dag i sjukskrivning") @QueryParam("daysIntoSickLeave") @DefaultValue("15") Integer daysIntoSickLeave,
        @ApiParam(value = "Svar på frågor") List<SrsQuestionResponse> questions) {
        authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_SRS).orThrow();
        LOG.debug("getSrs(intygId: {}, diagnosisCode: {}, prediktion: {}, atgard: {}, statistik: {}, daysIntoSickLeave, {})",
            intygId, diagnosisCode, prediktion, atgard, statistik, daysIntoSickLeave);
        try {
            SrsResponse srsResponse = srsService.getSrs(getWebCertUserService().getUser(), intygId, personnummer, diagnosisCode,
                prediktion, atgard, statistik, questions, daysIntoSickLeave);
            return Response.ok(srsResponse).build();
        } catch (InvalidPersonNummerException | IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/questions/{diagnosisCode}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get questions for diagnosis code", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "srs-get-question", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getQuestions(
        @ApiParam(value = "Diagnosis code") @PathParam("diagnosisCode") String diagnosisCode,
        @ApiParam(value = "Prediction model version") @QueryParam("modelVersion") String modelVersion
    ) {
        authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_SRS).orThrow();
        try {
            List<SrsQuestion> questionList = srsService.getQuestions(diagnosisCode, modelVersion);
            return Response.ok(questionList).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/consent/{personnummer}/{vardenhetHsaId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get consent for patient and careunit", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "srs-get-consent", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getConsent(
        @ApiParam(value = "Personnummer") @PathParam("personnummer") String personnummer,
        @ApiParam(value = "HsaId för vårdenhet") @PathParam("vardenhetHsaId") String careUnitHsaId) {
        authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_SRS).orThrow();
        try {
            Samtyckesstatus response = srsService.getConsent(careUnitHsaId, personnummer);
            return Response.ok(response).build();
        } catch (InvalidPersonNummerException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @PUT
    @Path("/consent/{personnummer}/{vardenhetHsaId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Set consent for patient and careunit", httpMethod = "PUT", produces = MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "srs-set-consent", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response setConsent(
        @ApiParam(value = "Personnummer") @PathParam("personnummer") String personnummer,
        @ApiParam(value = "HsaId för vårdenhet") @PathParam("vardenhetHsaId") String careUnitHsaId,
        boolean consent) {
        authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_SRS).orThrow();

        try {
            ResultCodeEnum result = srsService.setConsent(personnummer, careUnitHsaId, consent);
            return Response.ok(result).build();
        } catch (InvalidPersonNummerException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @PUT
    @Path("/opinion/{personnummer}/{vardgivareHsaId}/{vardenhetHsaId}/{intygId}/{diagnoskod}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Set own opinion for risk prediction", httpMethod = "PUT", produces = MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "srs-set-own-opinion", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response setOwnOpinion(
        @ApiParam(value = "Personnummer") @PathParam("personnummer") String personnummer,
        @ApiParam(value = "HSA-Id för vårdgivare") @PathParam("vardgivareHsaId") String vardgivareHsaId,
        @ApiParam(value = "HSA-Id för vårdenhet") @PathParam("vardenhetHsaId") String vardenhetHsaId,
        @ApiParam(value = "Intyg id", required = true) @PathParam("intygId") String intygId,
        @ApiParam(value = "Diagnoskod", required = true) @PathParam("diagnoskod") String diagnosisCode,
        String opinion) {
        authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_SRS).orThrow();

        try {
            ResultCodeEnum result = srsService.setOwnOpinion(personnummer, vardgivareHsaId, vardenhetHsaId, intygId,
                diagnosisCode, opinion);
            return Response.ok(result).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/codes")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "srs-get-diagnosis-codes", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getDiagnosisCodes(
        @ApiParam(value = "Prediction model version") @QueryParam("modelVersion") String modelVersion
    ) {
        authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_SRS).orThrow();
        return Response.ok(srsService.getAllDiagnosisCodes(modelVersion)).build();
    }

    @GET
    @Path("/atgarder/{diagnosisCode}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get SRS info for diagnosecode", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "srs-get-srs-for-diagnosis-codes", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getSrsForDiagnosisCodes(@PathParam("diagnosisCode") String diagnosisCode) {
        authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_SRS).orThrow();
        SrsForDiagnosisResponse srsForDiagnose = srsService.getSrsForDiagnosis(diagnosisCode);
        return Response.ok(srsForDiagnose).build();
    }

}
//CHECKSTYLE:ON ParameterNumber
