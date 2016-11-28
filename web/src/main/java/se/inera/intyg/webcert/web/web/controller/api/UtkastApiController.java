/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.*;

/**
 * API controller for REST services concerning certificate drafts.
 *
 * @author npet
 *
 */
@Path("/utkast")
@Api(value = "utkast", description = "REST API för utkasthantering", produces = MediaType.APPLICATION_JSON)
public class UtkastApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(UtkastApiController.class);

    private static final List<UtkastStatus> ALL_DRAFTS = Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_INCOMPLETE);
    private static final List<UtkastStatus> COMPLETE_DRAFTS = Collections.singletonList(UtkastStatus.DRAFT_COMPLETE);
    private static final List<UtkastStatus> INCOMPLETE_DRAFTS = Collections.singletonList(UtkastStatus.DRAFT_INCOMPLETE);

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    @Autowired
    private UtkastService intygDraftService;

    /**
     * Create a new draft.
     */
    @POST
    @Path("/{intygsTyp}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response createUtkast(@PathParam("intygsTyp") String intygsTyp, CreateUtkastRequest request) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
            .features(WebcertFeature.HANTERA_INTYGSUTKAST)
            .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
            .orThrow();

        if (!request.isValid()) {
            LOG.error("Request is invalid: " + request.toString());
            return Response.status(Status.BAD_REQUEST).build();
        }
        LOG.debug("Attempting to create draft of type '{}'", intygsTyp);

        CreateNewDraftRequest serviceRequest = createServiceRequest(request);

        Utkast utkast = intygDraftService.createNewDraft(serviceRequest);
        LOG.debug("Created a new draft of type '{}' with id '{}'", intygsTyp, utkast.getIntygsId());

        return Response.ok().entity(utkast).build();
    }

    @GET
    @Path("/questions/{intygsTyp}/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getQuestions(@PathParam("intygsTyp") String intygsTyp, @PathParam("version") String version) {

        LOG.debug("Requesting questions for '{}' with version '{}'.", intygsTyp, version);

        String questions = intygDraftService.getQuestions(intygsTyp, version);

        return Response.ok().entity(questions).build();
    }

    /**
     * Creates a filtered query to get drafts for a specific unit.
     */
    @GET
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response filterDraftsForUnit(@QueryParam("") QueryIntygParameter filterParameters) {

        authoritiesValidator.given(getWebCertUserService().getUser()).features(WebcertFeature.HANTERA_INTYGSUTKAST).orThrow();

        UtkastFilter utkastFilter = createUtkastFilter(filterParameters);
        QueryIntygResponse queryResponse = performUtkastFilterQuery(utkastFilter);

        return Response.ok(queryResponse).build();
    }

    /**
     * Returns a list of doctors that have one or more unsigned utkast.
     *
     * @return a list of {@link se.inera.intyg.webcert.web.service.dto.Lakare} objects.
     */
    @GET
    @Path("/lakare")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getLakareWithDraftsByEnheter() {

        authoritiesValidator.given(getWebCertUserService().getUser()).features(WebcertFeature.HANTERA_INTYGSUTKAST).orThrow();

        WebCertUser user = getWebCertUserService().getUser();
        String selectedUnitHsaId = user.getValdVardenhet().getId();

        List<Lakare> lakareWithDraftsByEnhet = intygDraftService.getLakareWithDraftsByEnhet(selectedUnitHsaId);

        return Response.ok().entity(lakareWithDraftsByEnhet).build();
    }

    private CreateNewDraftRequest createServiceRequest(CreateUtkastRequest req) {
        Patient pat = new Patient();
        pat.setPersonId(req.getPatientPersonnummer());
        pat.setFornamn(req.getPatientFornamn());
        pat.setMellannamn(req.getPatientMellannamn());
        pat.setEfternamn(req.getPatientEfternamn());
        pat.setFullstandigtNamn(IntygConverterUtil.concatPatientName(pat.getFornamn(), pat.getMellannamn(), pat.getEfternamn()));
        pat.setPostadress(req.getPatientPostadress());
        pat.setPostnummer(req.getPatientPostnummer());
        pat.setPostort(req.getPatientPostort());

        return new CreateNewDraftRequest(null, req.getIntygType(), null, createHoSPersonFromUser(), pat);
    }

    private UtkastFilter createUtkastFilter(QueryIntygParameter filterParameters) {
        WebCertUser user = getWebCertUserService().getUser();
        String selectedUnitHsaId = user.getValdVardenhet().getId();

        UtkastFilter utkastFilter = new UtkastFilter(selectedUnitHsaId);

        if (filterParameters != null) {
            if (Boolean.FALSE.equals(filterParameters.getComplete())) {
                utkastFilter.setStatusList(INCOMPLETE_DRAFTS);
            } else if (Boolean.TRUE.equals(filterParameters.getComplete())) {
                utkastFilter.setStatusList(COMPLETE_DRAFTS);
            } else {
                utkastFilter.setStatusList(ALL_DRAFTS);
            }

            utkastFilter.setSavedFrom(filterParameters.getSavedFrom());
            utkastFilter.setSavedTo(filterParameters.getSavedTo());
            utkastFilter.setSavedByHsaId(filterParameters.getSavedBy());
            utkastFilter.setNotified(filterParameters.getNotified());
            utkastFilter.setPageSize(filterParameters.getPageSize() == null ? DEFAULT_PAGE_SIZE : filterParameters.getPageSize());
            utkastFilter.setStartFrom(filterParameters.getStartFrom() == null ? 0 : filterParameters.getStartFrom());
        }

        return utkastFilter;
    }

    private QueryIntygResponse performUtkastFilterQuery(UtkastFilter filter) {

        List<Utkast> intygList = intygDraftService.filterIntyg(filter);

        List<ListIntygEntry> listIntygEntries = IntygDraftsConverter.convertUtkastsToListIntygEntries(intygList);

        int totalCountOfFilteredIntyg = intygDraftService.countFilterIntyg(filter);

        QueryIntygResponse response = new QueryIntygResponse(listIntygEntries);
        response.setTotalCount(totalCountOfFilteredIntyg);
        return response;
    }

}
