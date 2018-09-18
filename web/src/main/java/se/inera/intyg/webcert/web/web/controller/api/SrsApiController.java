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

import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.Samtyckesstatus;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.Utdatafilter;
import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.infra.integration.srs.model.SrsForDiagnosisResponse;
import se.inera.intyg.infra.integration.srs.model.SrsQuestion;
import se.inera.intyg.infra.integration.srs.model.SrsQuestionResponse;
import se.inera.intyg.infra.integration.srs.model.SrsResponse;
import se.inera.intyg.infra.integration.srs.services.SrsService;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponseType;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
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

    @Autowired
    private WebCertUserService userService;

    @Autowired
    private MonitoringLogService monitoringLog;

    @Autowired
    private DiagnosService diagnosService;

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
    public Response getSrs(@ApiParam(value = "Intyg id", required = true) @PathParam("intygId") String intygId,
            @ApiParam(value = "Personnummer", required = true) @PathParam("personnummer") String personnummer,
            @ApiParam(value = "Diagnosis Code", required = true) @PathParam("diagnosisCode") String diagnosisCode,
            @ApiParam(value = "Utdatafilter: Prediktion") @QueryParam("prediktion") @DefaultValue("false") boolean prediktion,
            @ApiParam(value = "Utdatafilter: AtgardRekommendation") @QueryParam("atgard") @DefaultValue("false") boolean atgard,
            @ApiParam(value = "Utdatafilter: Statistik") @QueryParam("statistik") @DefaultValue("false") boolean statistik,
            @ApiParam(value = "Svar på frågor") List<SrsQuestionResponse> questions) {
        authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_SRS).orThrow();

        if (Strings.isNullOrEmpty(personnummer) || Strings.isNullOrEmpty(diagnosisCode)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            Utdatafilter filter = buildUtdatafilter(prediktion, atgard, statistik);
            SrsResponse response = srsService
                    .getSrs(userService.getUser(), intygId, createPnr(personnummer), diagnosisCode, filter, questions);
            if (prediktion) {
                logService.logShowPrediction(personnummer);
                monitoringLog.logSrsInformationRetreived(diagnosisCode, intygId);
            }
            decorateWithDiagnosisDescription(response);
            return Response.ok(response).build();
        } catch (InvalidPersonNummerException | IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/questions/{diagnosisCode}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get questions for diagnosis code", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    public Response getQuestions(@ApiParam(value = "Diagnosis code") @PathParam("diagnosisCode") String diagnosisCode) {
        authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_SRS).orThrow();

        if (Strings.isNullOrEmpty(diagnosisCode)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        List<SrsQuestion> response = srsService.getQuestions(diagnosisCode);
        monitoringLog.logListSrsQuestions(diagnosisCode);
        return Response.ok(response).build();
    }

    @GET
    @Path("/consent/{personnummer}/{hsaId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get consent for patient and careunit", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    public Response getConsent(
            @ApiParam(value = "Personnummer") @PathParam("personnummer") String personnummer,
            @ApiParam(value = "HsaId för vårdenhet") @PathParam("hsaId") String hsaId) {
        authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_SRS).orThrow();

        try {
            Personnummer p = createPnr(personnummer);
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
    @PrometheusTimeMethod
    public Response setConsent(
            @ApiParam(value = "Personnummer") @PathParam("personnummer") String personnummer,
            @ApiParam(value = "HsaId för vårdenhet") @PathParam("hsaId") String hsaId,
            boolean consent) {
        authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_SRS).orThrow();

        try {
            Personnummer p = createPnr(personnummer);
            ResultCodeEnum result = srsService.setConsent(hsaId, p, consent);
            monitoringLog.logSetSrsConsent(p, consent);
            return Response.ok(result).build();
        } catch (InvalidPersonNummerException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/codes")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getDiagnosisCodes() {
        authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_SRS).orThrow();
        return Response.ok(srsService.getAllDiagnosisCodes()).build();
    }

    @GET
    @Path("/atgarder/{diagnosisCode}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get SRS info for diagnosecode", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    public Response getSrsForDiagnosisCodes(@PathParam("diagnosisCode") String diagnosisCode) {
        authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_SRS).orThrow();

        final SrsForDiagnosisResponse srsForDiagnose = srsService.getSrsForDiagnose(diagnosisCode);
        monitoringLog.logGetSrsForDiagnose(diagnosisCode);

        return Response.ok(srsForDiagnose).build();
    }

    private Utdatafilter buildUtdatafilter(boolean prediktion, boolean atgard, boolean statistik) {
        Utdatafilter filter = new Utdatafilter();
        filter.setPrediktion(prediktion);
        filter.setAtgardsrekommendation(atgard);
        filter.setStatistik(statistik);
        return filter;
    }

    private void decorateWithDiagnosisDescription(SrsResponse response) {
        if (!Strings.isNullOrEmpty(response.getPredictionDiagnosisCode())) {
            DiagnosResponse diagnosResponse = diagnosService
                    .getDiagnosisByCode(response.getPredictionDiagnosisCode(), Diagnoskodverk.ICD_10_SE);
            if (diagnosResponse.getResultat() == DiagnosResponseType.OK && diagnosResponse.getDiagnoser() != null
                    && !diagnosResponse.getDiagnoser().isEmpty()) {
                response.setPredictionDiagnosisDescription(diagnosResponse.getDiagnoser().get(0).getBeskrivning());
            }
        }
        if (!Strings.isNullOrEmpty(response.getAtgarderDiagnosisCode())) {
            DiagnosResponse diagnosResponse = diagnosService
                    .getDiagnosisByCode(response.getAtgarderDiagnosisCode(), Diagnoskodverk.ICD_10_SE);
            if (diagnosResponse.getResultat() == DiagnosResponseType.OK && diagnosResponse.getDiagnoser() != null
                    && !diagnosResponse.getDiagnoser().isEmpty()) {
                response.setAtgarderDiagnosisDescription(diagnosResponse.getDiagnoser().get(0).getBeskrivning());
            }
        }
        if (!Strings.isNullOrEmpty(response.getStatistikDiagnosisCode())) {
            DiagnosResponse diagnosResponse = diagnosService
                    .getDiagnosisByCode(response.getStatistikDiagnosisCode(), Diagnoskodverk.ICD_10_SE);
            if (diagnosResponse.getResultat() == DiagnosResponseType.OK && diagnosResponse.getDiagnoser() != null
                    && !diagnosResponse.getDiagnoser().isEmpty()) {
                response.setStatistikDiagnosisDescription(diagnosResponse.getDiagnoser().get(0).getBeskrivning());
            }
        }
    }

    private Personnummer createPnr(String personId) throws InvalidPersonNummerException {
        return Personnummer.createPersonnummer(personId)
                .orElseThrow(() -> new InvalidPersonNummerException("Could not parse personnummer: " + personId));
    }

}
//CHECKSTYLE:ON ParameterNumber
