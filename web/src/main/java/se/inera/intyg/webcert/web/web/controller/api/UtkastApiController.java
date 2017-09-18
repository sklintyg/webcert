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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygResponse;

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

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

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

        final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(request.getPatientPersonnummer());
        if (SekretessStatus.UNDEFINED.equals(sekretessStatus)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                    "Could not fetch sekretesstatus for patient from PU service");
        }
        // INTYG-4086: If the patient is sekretessmarkerad, we need an additional check.
        boolean sekr = sekretessStatus == SekretessStatus.TRUE;
        if (sekr) {
            authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                    .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT)
                    .orThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING,
                            "User missing required privilege or cannot handle sekretessmarkerad patient"));
        }

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
        Patient pat = patientDetailsResolver.resolvePatient(req.getPatientPersonnummer(), req.getIntygType());

        // Ugly, but null (for now) means that the PU service was not available or that the standardized logic in the
        // resolver asks the calling code to fall-back to "manual entry". In this case, the stuff from the
        // CreateUtkastRequest is the manual entry.
        if (pat == null) {
            pat = new Patient();
            pat.setPersonId(req.getPatientPersonnummer());
            pat.setFornamn(req.getPatientFornamn());
            pat.setMellannamn(req.getPatientMellannamn());
            pat.setEfternamn(req.getPatientEfternamn());
            pat.setFullstandigtNamn(IntygConverterUtil.concatPatientName(pat.getFornamn(), pat.getMellannamn(), pat.getEfternamn()));
            pat.setPostadress(req.getPatientPostadress());
            pat.setPostnummer(req.getPatientPostnummer());
            pat.setPostort(req.getPatientPostort());
        }

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

        // INTYG-4486: We can not get a totalCount with pageSize set if since we need to lookup/verify
        // sekretess!=UNDEFINED each entry from puService - even if user has authority to view sekretessmarkerade
        // resources.
        Integer pageSize = filter.getPageSize();

        filter.setPageSize(null);

        List<ListIntygEntry> listIntygEntries = IntygDraftsConverter
                .convertUtkastsToListIntygEntries(intygDraftService.filterIntyg(filter));

        // INTYG-4486, INTYG-4086: Always filter out any items with UNDEFINED sekretessmarkering status and not
        // authorized
        final WebCertUser user = getWebCertUserService().getUser();
        listIntygEntries = listIntygEntries.stream()
                .filter(lie -> this.passesSekretessCheck(lie.getPatientId(), lie.getIntygType(), user))
                .collect(Collectors.toList());

        // INTYG-4086: Mark all remaining ListIntygEntry having a patient with sekretessmarkering
        listIntygEntries.stream().forEach(this::markSekretessMarkering);

        int totalCountOfFilteredIntyg = listIntygEntries.size();

        // Now that we have filtered for sekretess == UNDEFINED and sekretess authorization - we apply the pagination
        // logic
        if (filter.getStartFrom() < listIntygEntries.size()) {
            int toIndex = filter.getStartFrom() + pageSize;
            if (toIndex > listIntygEntries.size()) {
                toIndex = listIntygEntries.size();
            }
            listIntygEntries = listIntygEntries.subList(filter.getStartFrom(), toIndex);
        } else {
            // Index out of range
            listIntygEntries.clear();
        }

        QueryIntygResponse response = new QueryIntygResponse(listIntygEntries);
        response.setTotalCount(totalCountOfFilteredIntyg);
        return response;
    }


    private boolean passesSekretessCheck(Personnummer patientId, String intygsTyp, WebCertUser user) {
        final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(patientId);

        if (sekretessStatus == SekretessStatus.UNDEFINED) {
            //No matter if user has
            return false;
        } else {
            return sekretessStatus == SekretessStatus.FALSE || authoritiesValidator.given(user, intygsTyp)
                    .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT)
                    .isVerified();
        }

    }

    private void markSekretessMarkering(ListIntygEntry lie) {
        if (patientDetailsResolver.getSekretessStatus(lie.getPatientId()) == SekretessStatus.TRUE) {
            lie.setSekretessmarkering(true);
        }
    }

}
