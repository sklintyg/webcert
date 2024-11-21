/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.moduleapi;

import io.swagger.annotations.Api;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.FragaSvarView;
import se.inera.intyg.webcert.web.web.controller.api.dto.QARequest;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.CreateQuestionParameter;

@Path("/fragasvar")
@Api(value = "fragasvar", produces = MediaType.APPLICATION_JSON)
public class FragaSvarModuleApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(FragaSvarModuleApiController.class);

    @Autowired
    private FragaSvarService fragaSvarService;

    @GET
    @Path("/{intygsTyp}/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "fragasvar-module-fragasvar-for-certificate", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public List<FragaSvarView> fragaSvarForIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId) {
        abortIfFragaSvarNotActive(intygsTyp);
        return fragaSvarService.getFragaSvar(intygsId);
    }

    @PUT
    @Path("/{intygsTyp}/{fragasvarId}/besvara")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "fragasvar-module-answer-with-type", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response answer(@PathParam("intygsTyp") String intygsTyp, @PathParam("fragasvarId") final Long frageSvarId, String svarsText) {
        abortIfFragaSvarNotActive(intygsTyp);
        LOG.debug("Set answer for question {}", frageSvarId);
        FragaSvar fragaSvarResponse = fragaSvarService.saveSvar(frageSvarId, svarsText);
        return Response.ok(fragaSvarResponse).build();
    }

    @PUT
    @Path("/{intygsId}/besvara")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "fragasvar-module-answer-without-typ", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response answer(@PathParam("intygsId") final String intygsId, final String svarsText) {
        LOG.debug("Answer arenden for intyg {}", intygsId);
        final List<FragaSvarView> response = fragaSvarService.answerKomplettering(intygsId, svarsText);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{intygsId}/vidarebefordrad")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "fragasvar-module-set-dispatch-state", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response setDispatchState(@PathParam("intygsId") final String intygsId) {
        LOG.debug("Set vidarebefordra for all frågasvar related to IntygsId {}", intygsId);
        return Response.ok(fragaSvarService.setVidareBefordrad(intygsId)).build();
    }

    @POST
    @Path("/{intygsTyp}/{intygsId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "fragasvar-module-create-question", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response createQuestion(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") final String intygsId,
        CreateQuestionParameter parameter) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
            .features(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR, AuthoritiesConstants.FEATURE_SKAPA_NYFRAGA)
            .orThrow();

        LOG.debug("New question for cert {} with subject {}", intygsId, parameter.getAmne());
        FragaSvar fragaSvarResponse = fragaSvarService.saveNewQuestion(intygsId, intygsTyp, parameter.getAmne(), parameter.getFrageText());
        return Response.ok(fragaSvarResponse).build();
    }

    @GET
    @Path("/{intygsTyp}/{fragasvarId}/stang")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "fragasvar-module-close-as-handled", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public FragaSvar closeAsHandled(@PathParam("intygsTyp") String intygsTyp, @PathParam("fragasvarId") Long fragasvarId) {
        abortIfFragaSvarNotActive(intygsTyp);
        return fragaSvarService.closeQuestionAsHandled(fragasvarId);
    }

    @PUT
    @Path("/stang")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "fragasvar-module-close-qas-as-handled", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public List<FragaSvar> closeQAsAsHandled(List<QARequest> qas) {
        List<FragaSvar> fragaSvars = new ArrayList<>();
        for (QARequest qa : qas) {
            abortIfFragaSvarNotActive(qa.getIntygsTyp());
            fragaSvars.add(fragaSvarService.closeQuestionAsHandled(qa.getFragaSvarId()));
        }
        return fragaSvars;
    }

    @GET
    @Path("/{intygsTyp}/{fragasvarId}/oppna")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "fragasvar-module-open-as-unhandled", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public FragaSvar openAsUnhandled(@PathParam("intygsTyp") String intygsTyp, @PathParam("fragasvarId") Long fragasvarId) {
        abortIfFragaSvarNotActive(intygsTyp);
        return fragaSvarService.openQuestionAsUnhandled(fragasvarId);
    }

    private void abortIfFragaSvarNotActive(String intygsTyp) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp).features(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
            .orThrow();
    }

}
