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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.AdditionType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.GetCertificateAdditionsResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.GetCertificateAdditionsType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.IntygAdditionsType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.StatusType;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.util.StreamUtil;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IIType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller to get access to 'kompletteringar'.
 */
@Path("/arende")
@Api(value = "komplettering", description = "REST API för kompletteringar", produces = MediaType.APPLICATION_JSON)
public class ArendeApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(ArendeApiController.class);

    static final int OK = 200;
    static final int NO_CONTENT = 204;
    static final int BAD_REQUEST = 400;

    @Autowired
    private ArendeService arendeService;

    @POST
    @Path("/kompletteringar")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get complementary data", httpMethod = "POST", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = OK, message = "Complementary data found", response = GetCertificateAdditionsResponseType.class),
            @ApiResponse(code = BAD_REQUEST, message = "Bad request"),
            @ApiResponse(code = NO_CONTENT, message = "No complementary data found")
    })
    @PrometheusTimeMethod
    public Response getKompletteringar(GetCertificateAdditionsType request) {
        if (isValidRequest(request)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        LocalTime start = LocalTime.now();

        GetCertificateAdditionsResponseType response = new GetCertificateAdditionsResponseType();
        response.getAdditions().addAll(new ArrayList<>());

        // Do authority check
        authoritiesValidator.given(getWebCertUserService().getUser())
                .privilege(AuthoritiesConstants.PRIVILEGE_VISA_INTYG)
                .orThrow();

        try {
            List<IntygId> identifiers = request.getIntygsId().stream()
                    .filter(StreamUtil.distinctByKeys(IIType::getExtension))
                    .collect(Collectors.toList());

            List<String> extensions = identifiers.stream()
                    .map(IIType::getExtension)
                    .collect(Collectors.toList());

            List<Arende> kompletteringar = arendeService.getKompletteringar(extensions);
            if (kompletteringar == null || kompletteringar.isEmpty()) {
                return Response.noContent().entity(response).build();
            }

            identifiers.forEach(identity ->
                    response.getAdditions().add(buildIntygAdditionsType(identity, kompletteringar)));

            response.setResult(ResultCodeType.OK);

            LOG.debug("ArendeApiController: Successfully returned {} komplettering in {} seconds",
                    response.getAdditions().stream().map(IntygAdditionsType::getAddition).mapToLong(List::size).sum(),
                    getExecutionTime(start));
            return Response.ok(response).build();
        } catch (Exception e) {
            LOG.error("ArendeApiController: Failed returning kompletteringar", e);
            response.setResult(ResultCodeType.ERROR);
            return Response.serverError().entity(response).build();
        }
    }

    private boolean isValidRequest(GetCertificateAdditionsType request) {
        return request == null || request.getIntygsId() == null || request.getIntygsId().isEmpty();
    }

    private String getExecutionTime(LocalTime start) {
        return LocalTime.now()
                .minus(start.toNanoOfDay(), ChronoUnit.NANOS)
                .format(DateTimeFormatter.ofPattern("ss.SSS"));
    }

    private IntygAdditionsType buildIntygAdditionsType(IntygId intygId,
                                                     List<Arende> kompletteringar) {

        List<AdditionType> additions = kompletteringar.stream()
                .filter(kmplt -> kmplt.getIntygsId().equals(intygId.getExtension()))
                .map(this::mapArende)
                .collect(Collectors.toList());

        IntygAdditionsType intygAdditionsType = new IntygAdditionsType();
        intygAdditionsType.setIntygsId(intygId);
        intygAdditionsType.getAddition().addAll(additions);

        return intygAdditionsType;
    }

    private AdditionType mapArende(Arende arende) {
        AdditionType additionType = new AdditionType();
        additionType.setId(String.valueOf(arende.getId()));
        additionType.setSkapad(arende.getTimestamp());
        additionType.setStatus(mapStatus(arende.getStatus()));

        return additionType;
    }

    private StatusType mapStatus(Status status) {
        switch (status) {
            case CLOSED:
                return StatusType.BESVARAD;
            default:
                return StatusType.OBESVARAD;
        }
    }

}
