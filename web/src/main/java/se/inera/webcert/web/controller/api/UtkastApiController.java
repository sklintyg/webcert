package se.inera.webcert.web.controller.api;

import java.util.Arrays;
import java.util.List;

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

import se.inera.webcert.converter.IntygDraftsConverter;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.service.dto.Lakare;
import se.inera.webcert.service.dto.Patient;
import se.inera.webcert.service.feature.WebcertFeature;
import se.inera.webcert.service.utkast.UtkastService;
import se.inera.webcert.service.utkast.dto.CreateNewDraftRequest;
import se.inera.webcert.web.controller.AbstractApiController;
import se.inera.webcert.web.controller.api.dto.CreateUtkastRequest;
import se.inera.webcert.web.controller.api.dto.ListIntygEntry;
import se.inera.webcert.web.controller.api.dto.QueryIntygParameter;
import se.inera.webcert.web.controller.api.dto.QueryIntygResponse;

/**
 * API controller for REST services concerning certificate drafts.
 *
 * @author npet
 *
 */
@Path("/utkast")
public class UtkastApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(UtkastApiController.class);

    private static final List<UtkastStatus> ALL_DRAFTS = Arrays.asList(UtkastStatus.DRAFT_COMPLETE,
            UtkastStatus.DRAFT_INCOMPLETE);

    private static final List<UtkastStatus> COMPLETE_DRAFTS = Arrays.asList(UtkastStatus.DRAFT_COMPLETE);

    private static final List<UtkastStatus> INCOMPLETE_DRAFTS = Arrays.asList(UtkastStatus.DRAFT_INCOMPLETE);

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    @Autowired
    private UtkastService intygDraftService;

    @Autowired
    private UtkastRepository utkastRepository;

    /**
     * Creates a filtered query to get drafts for a specific unit.
     *
     * @param filterParameters
     * @return
     */
    @GET
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response filterDraftsForUnit(@QueryParam("") QueryIntygParameter filterParameters) {

        abortIfWebcertFeatureIsNotAvailable(WebcertFeature.HANTERA_INTYGSUTKAST);

        UtkastFilter utkastFilter = createUtkastFilter(filterParameters);
        QueryIntygResponse queryResponse = performUtkastFilterQuery(utkastFilter);

        return Response.ok(queryResponse).build();
    }

    private UtkastFilter createUtkastFilter(QueryIntygParameter filterParameters) {
        WebCertUser user = getWebCertUserService().getWebCertUser();
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
            utkastFilter.setForwarded(filterParameters.getForwarded());
            utkastFilter.setPageSize(filterParameters.getPageSize() == null ? DEFAULT_PAGE_SIZE : filterParameters.getPageSize());
            utkastFilter.setStartFrom(filterParameters.getStartFrom() == null ? 0 : filterParameters.getStartFrom());
        }

        return utkastFilter;
    }

    private QueryIntygResponse performUtkastFilterQuery(UtkastFilter filter) {

        List<Utkast> intygList = utkastRepository.filterIntyg(filter);

        List<ListIntygEntry> listIntygEntries = IntygDraftsConverter.convertUtkastsToListIntygEntries(intygList);

        int totalCountOfFilteredIntyg = utkastRepository.countFilterIntyg(filter);

        QueryIntygResponse response = new QueryIntygResponse(listIntygEntries);
        response.setTotalCount(totalCountOfFilteredIntyg);
        return response;
    }

    /**
     * Returns a list of doctors that have one or more unsigned utkast.
     *
     * @return a list of {@link se.inera.webcert.service.dto.Lakare} objects.
     */
    @GET
    @Path("/lakare")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getLakareWithDraftsByEnheter() {

        abortIfWebcertFeatureIsNotAvailable(WebcertFeature.HANTERA_INTYGSUTKAST);

        WebCertUser user = getWebCertUserService().getWebCertUser();
        String selectedUnitHsaId = user.getValdVardenhet().getId();

        List<Lakare> lakareWithDraftsByEnhet = intygDraftService.getLakareWithDraftsByEnhet(selectedUnitHsaId);

        return Response.ok().entity(lakareWithDraftsByEnhet).build();
    }

    /**
     * Create a new draft.
     *
     * @param intygsTyp
     * @param request
     * @return
     */
    @POST
    @Path("/{intygsTyp}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN + UTF_8_CHARSET)
    public Response createUtkast(@PathParam("intygsTyp") String intygsTyp, CreateUtkastRequest request) {

        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);

        if (!request.isValid()) {
            LOG.error("Request is invalid: " + request.toString());
            return Response.status(Status.BAD_REQUEST).build();
        }

        LOG.debug("Attempting to create draft of type '{}'", intygsTyp);

        CreateNewDraftRequest serviceRequest = createServiceRequest(request);

        String utkastId = intygDraftService.createNewDraft(serviceRequest); 

        LOG.debug("Created a new draft of type '{}' with id '{}'", intygsTyp, utkastId);

        return Response.ok().entity(utkastId).build();
    }

    private CreateNewDraftRequest createServiceRequest(CreateUtkastRequest req) {
        CreateNewDraftRequest srvReq = new CreateNewDraftRequest();

        srvReq.setIntygType(req.getIntygType());

        Patient pat = new Patient();
        pat.setPersonnummer(req.getPatientPersonnummer());
        pat.setFornamn(req.getPatientFornamn());
        pat.setMellannamn(req.getPatientMellannamn());
        pat.setEfternamn(req.getPatientEfternamn());
        pat.setPostadress(req.getPatientPostadress());
        pat.setPostnummer(req.getPatientPostnummer());
        pat.setPostort(req.getPatientPostort());
        srvReq.setPatient(pat);

        srvReq.setHosPerson(createHoSPersonFromUser());
        srvReq.setVardenhet(createVardenhetFromUser());

        return srvReq;
    }
}
