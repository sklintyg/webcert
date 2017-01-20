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
package se.inera.intyg.webcert.web.web.controller.moduleapi;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.FragaSvarView;
import se.inera.intyg.webcert.web.web.controller.api.dto.QARequest;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.CreateQuestionParameter;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DispatchState;

@Path("/fragasvar")
@Api(value = "fragasvar", description = "REST API - moduleapi - fragasvar", produces = MediaType.APPLICATION_JSON)
public class FragaSvarModuleApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(FragaSvarModuleApiController.class);

    @Autowired
    private FragaSvarService fragaSvarService;

    @GET
    @Path("/{intygsTyp}/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public List<FragaSvarView> fragaSvarForIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId) {
        abortIfFragaSvarNotActive(intygsTyp);
        return fragaSvarService.getFragaSvar(intygsId);
    }

    @PUT
    @Path("/{intygsTyp}/{fragasvarId}/besvara")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response answer(@PathParam("intygsTyp") String intygsTyp, @PathParam("fragasvarId") final Long frageSvarId, String svarsText) {
        abortIfFragaSvarNotActive(intygsTyp);
        LOG.debug("Set answer for question {}", frageSvarId);
        FragaSvar fragaSvarResponse = fragaSvarService.saveSvar(frageSvarId, svarsText);
        return Response.ok(fragaSvarResponse).build();
    }

    @PUT
    @Path("/{intygsTyp}/{fragasvarId}/hanterad")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response setDispatchState(@PathParam("intygsTyp") String intygsTyp, @PathParam("fragasvarId") final Long frageSvarId,
            DispatchState dispatchState) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(WebcertFeature.HANTERA_FRAGOR)
                .privilege(AuthoritiesConstants.PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR)
                .orThrow();

        LOG.debug("Set DispatchState for question {}, isDispatched: {}", frageSvarId, dispatchState.isDispatched());
        FragaSvar fragaSvarResponse = fragaSvarService.setDispatchState(frageSvarId, dispatchState.isDispatched());
        return Response.ok(fragaSvarResponse).build();
    }

    @POST
    @Path("/{intygsTyp}/{intygsId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response createQuestion(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") final String intygsId,
            CreateQuestionParameter parameter) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(WebcertFeature.HANTERA_FRAGOR, WebcertFeature.SKAPA_NYFRAGA)
                .orThrow();

        LOG.debug("New question for cert {} with subject {}", intygsId, parameter.getAmne());
        FragaSvar fragaSvarResponse = fragaSvarService.saveNewQuestion(intygsId, intygsTyp, parameter.getAmne(), parameter.getFrageText());
        return Response.ok(fragaSvarResponse).build();
    }

    @GET
    @Path("/{intygsTyp}/{fragasvarId}/stang")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public FragaSvar closeAsHandled(@PathParam("intygsTyp") String intygsTyp, @PathParam("fragasvarId") Long fragasvarId) {
        abortIfFragaSvarNotActive(intygsTyp);
        return fragaSvarService.closeQuestionAsHandled(fragasvarId);
    }

    @PUT
    @Path("/stang")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
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
    public FragaSvar openAsUnhandled(@PathParam("intygsTyp") String intygsTyp, @PathParam("fragasvarId") Long fragasvarId) {
        abortIfFragaSvarNotActive(intygsTyp);
        return fragaSvarService.openQuestionAsUnhandled(fragasvarId);
    }

    private void abortIfFragaSvarNotActive(String intygsTyp) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp).features(WebcertFeature.HANTERA_FRAGOR).orThrow();
    }
}
