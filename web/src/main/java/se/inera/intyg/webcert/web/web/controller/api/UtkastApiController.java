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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.access.AccessService;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygResponse;

/**
 * API controller for REST services concerning certificate drafts.
 *
 * @author npet
 */
@Path("/utkast")
@Api(value = "utkast", description = "REST API för utkasthantering", produces = MediaType.APPLICATION_JSON)
public class UtkastApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(UtkastApiController.class);

    private static final List<UtkastStatus> ALL_DRAFTS = Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_INCOMPLETE,
            UtkastStatus.DRAFT_LOCKED);

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    @Autowired
    private UtkastService utkastService;

    @Autowired
    private IntygTextsService intygTextsService;

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private AccessService accessService;

    /**
     * Create a new draft.
     */
    @POST
    @Path("/{intygsTyp}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response createUtkast(@PathParam("intygsTyp") String intygsTyp, CreateUtkastRequest request) {
        try {
            if (moduleRegistry.getIntygModule(intygsTyp).isDeprecated()) {
                LOG.error("Request for deprecated module {}", intygsTyp);
                return Response.status(Status.BAD_REQUEST).build();
            }
        } catch (ModuleNotFoundException e) {
            LOG.error("Request for unknown module {}", intygsTyp);
            return Response.status(Status.BAD_REQUEST).build();
        }

        if (!request.isValid()) {
            LOG.error("Request is invalid: " + request.toString());
            return Response.status(Status.BAD_REQUEST).build();
        }
        LOG.debug("Attempting to create draft of type '{}'", intygsTyp);

        final boolean allowedToCreateUtkast = accessService.allowedToCreateUtkast(intygsTyp, request.getPatientPersonnummer());
        if (!allowedToCreateUtkast) {
            // TODO: Manage so correct exception can be thrown
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "Not allowed to create utkast");
        }

        CreateNewDraftRequest serviceRequest = createServiceRequest(request);

        Utkast utkast = utkastService.createNewDraft(serviceRequest);
        LOG.debug("Created a new draft of type '{}' with id '{}'", intygsTyp, utkast.getIntygsId());

        return Response.ok().entity(utkast).build();
    }

    @GET
    @Path("/questions/{intygsTyp}/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getQuestions(@PathParam("intygsTyp") String intygsTyp, @PathParam("version") String version) {

        LOG.debug("Requesting questions for '{}' with version '{}'.", intygsTyp, version);

        String questions = utkastService.getQuestions(intygsTyp, version);

        return Response.ok().entity(questions).build();
    }

    /**
     * Creates a filtered query to get drafts for a specific unit.
     */
    @GET
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response filterDraftsForUnit(@QueryParam("") QueryIntygParameter filterParameters) {

        authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST).orThrow();

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
    @PrometheusTimeMethod
    public Response getLakareWithDraftsByEnheter() {

        authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST).orThrow();

        WebCertUser user = getWebCertUserService().getUser();
        String selectedUnitHsaId = user.getValdVardenhet().getId();

        List<Lakare> lakareWithDraftsByEnhet = utkastService.getLakareWithDraftsByEnhet(selectedUnitHsaId);

        return Response.ok().entity(lakareWithDraftsByEnhet).build();
    }

    @GET
    @Path("/previousIntyg/{personnummer}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getPreviousCertificateWarnings(@PathParam("personnummer") String personnummer) {
        Map<String, Map<String, PreviousIntyg>> res = utkastService
                .checkIfPersonHasExistingIntyg(Personnummer.createPersonnummer(personnummer).get(),
                        getWebCertUserService().getUser());
        return Response.ok(res).build();
    }

    private CreateNewDraftRequest createServiceRequest(CreateUtkastRequest req) {
        String latestIntygTypeVersion = intygTextsService.getLatestVersion(req.getIntygType());
        Patient pat = patientDetailsResolver.resolvePatient(req.getPatientPersonnummer(), req.getIntygType(), latestIntygTypeVersion);

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
        return new CreateNewDraftRequest(null, req.getIntygType(), latestIntygTypeVersion, null, createHoSPersonFromUser(), pat);
    }

    private UtkastFilter createUtkastFilter(QueryIntygParameter filterParameters) {
        WebCertUser user = getWebCertUserService().getUser();
        String selectedUnitHsaId = user.getValdVardenhet().getId();

        UtkastFilter utkastFilter = new UtkastFilter(selectedUnitHsaId);

        if (filterParameters != null) {
            if (filterParameters.getStatus() == null) {
                utkastFilter.setStatusList(ALL_DRAFTS);
            } else {
                utkastFilter.setStatusList(Arrays.asList(filterParameters.getStatus()));
            }

            utkastFilter.setSavedFrom(filterParameters.getSavedFrom());
            utkastFilter.setSavedTo(filterParameters.getSavedTo());
            utkastFilter.setSavedByHsaId(filterParameters.getSavedBy());
            utkastFilter.setNotified(filterParameters.getNotified());
            utkastFilter.setPageSize(filterParameters.getPageSize() == null ? DEFAULT_PAGE_SIZE : filterParameters.getPageSize());
            utkastFilter.setStartFrom(filterParameters.getStartFrom() == null ? 0 : filterParameters.getStartFrom());
            utkastFilter.setOrderBy(filterParameters.getOrderBy() == null ? "" : filterParameters.getOrderBy());
            utkastFilter.setOrderAscending(filterParameters.getOrderAscending() == null ? false : filterParameters.getOrderAscending());
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
                .convertUtkastsToListIntygEntries(utkastService.filterIntyg(filter),
                        getIntygComparator(filter.getOrderBy(), filter.getOrderAscending()));

        // INTYG-4486, INTYG-4086: Always filter out any items with UNDEFINED sekretessmarkering status and not
        // authorized
        Map<Personnummer, SekretessStatus> sekretessStatusMap = patientDetailsResolver.getSekretessStatusForList(listIntygEntries.stream()
                .map(lie -> lie.getPatientId())
                .collect(Collectors.toList()));

        final WebCertUser user = getWebCertUserService().getUser();
        listIntygEntries = listIntygEntries.stream()
                .filter(lie -> this.passesSekretessCheck(lie.getPatientId(), lie.getIntygType(), user, sekretessStatusMap))
                .collect(Collectors.toList());

        // INTYG-4086: Mark all remaining ListIntygEntry having a patient with sekretessmarkering
        listIntygEntries.stream().forEach(lie -> markSekretessMarkering(lie, sekretessStatusMap));

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

    private Comparator<ListIntygEntry> getIntygComparator(String orderBy, Boolean ascending) {
        Comparator<ListIntygEntry> comparator;
        switch (orderBy) {
        case "intygsTyp":
            comparator = Comparator.comparing(ListIntygEntry::getIntygType);
            break;
        case "status":
            comparator = Comparator.comparing(ListIntygEntry::getStatus);
            break;
        case "patientPersonnummer":
            comparator = (ie1, ie2) -> ie2.getPatientId().toString()
                    .compareTo(ie1.getPatientId().toString());
            break;
        case "senastSparadAv":
            comparator = Comparator.comparing(ListIntygEntry::getUpdatedSignedBy);
            break;
        case "vidarebefordrad":
            comparator = (ie1, ie2) -> Boolean.compare(ie2.isVidarebefordrad(), ie1.isVidarebefordrad());
            break;
        case "senasteSparadDatum":
        default:
            comparator = Comparator.comparing(ListIntygEntry::getLastUpdatedSigned);
            break;
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private boolean passesSekretessCheck(Personnummer patientId, String intygsTyp, WebCertUser user,
            Map<Personnummer, SekretessStatus> sekretessStatusMap) {
        final SekretessStatus sekretessStatus = sekretessStatusMap.get(patientId);

        if (sekretessStatus == SekretessStatus.UNDEFINED) {
            // No matter if user has
            return false;
        } else {
            return sekretessStatus == SekretessStatus.FALSE || authoritiesValidator.given(user, intygsTyp)
                    .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT)
                    .isVerified();
        }

    }

    private void markSekretessMarkering(ListIntygEntry lie, Map<Personnummer, SekretessStatus> sekretessStatusMap) {
        if (sekretessStatusMap.get(lie.getPatientId()) == SekretessStatus.TRUE) {
            lie.setSekretessmarkering(true);
        }
    }

}
