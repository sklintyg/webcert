package se.inera.webcert.web.controller.api;

import java.util.Arrays;
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
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.converter.IntygDraftsConverter;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.modules.IntygModule;
import se.inera.webcert.modules.IntygModuleRegistry;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.repository.IntygFilter;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.IntygService;
import se.inera.webcert.service.draft.IntygDraftService;
import se.inera.webcert.service.draft.dto.CreateNewDraftRequest;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.dto.IntygItem;
import se.inera.webcert.service.dto.Lakare;
import se.inera.webcert.service.dto.Patient;
import se.inera.webcert.service.dto.Vardenhet;
import se.inera.webcert.service.dto.Vardgivare;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.web.controller.AbstractApiController;
import se.inera.webcert.web.controller.api.dto.CreateNewIntygRequest;
import se.inera.webcert.web.controller.api.dto.ListIntygEntry;
import se.inera.webcert.web.controller.api.dto.QueryIntygParameter;
import se.inera.webcert.web.controller.api.dto.QueryIntygResponse;

/**
 * Controller for the API that serves WebCert.
 * 
 * @author nikpet
 * 
 */
@Path("/intyg")
public class IntygApiController extends AbstractApiController {

    private static Logger LOG = LoggerFactory.getLogger(IntygApiController.class);

    private static List<IntygsStatus> ALL_DRAFTS = Arrays.asList(IntygsStatus.DRAFT_COMPLETE,
            IntygsStatus.DRAFT_INCOMPLETE);
    
    private static List<IntygsStatus> COMPLETE_DRAFTS = Arrays.asList(IntygsStatus.DRAFT_COMPLETE);
    
    private static List<IntygsStatus> INCOMPLETE_DRAFTS = Arrays.asList(IntygsStatus.DRAFT_INCOMPLETE);
    
    @Autowired
    private IntygService intygService;

    @Autowired
    private IntygRepository intygRepository;

    @Autowired
    private IntygDraftService intygDraftService;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private LogService logService;

    public IntygApiController() {

    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response createNewIntyg(CreateNewIntygRequest request) {

        if (!request.isValid()) {
            LOG.error("Request is invalid: " + request.toString());
            return Response.status(Status.BAD_REQUEST).build();
        }

        String intygType = request.getIntygType();

        LOG.debug("Attempting to create draft of type '{}'", intygType);

        CreateNewDraftRequest serviceRequest = createServiceRequest(request);

        String idOfCreatedDraft = intygDraftService.createNewDraft(serviceRequest);

        LOG.debug("Created a new draft of type '{}' with id '{}'", intygType, idOfCreatedDraft);

        logCreateOfIntyg(idOfCreatedDraft, request);

        return Response.ok().entity(idOfCreatedDraft).build();
    }

    private void logCreateOfIntyg(String idOfCreatedDraft, CreateNewIntygRequest request) {

        LogRequest logRequest = new LogRequest();
        logRequest.setIntygId(idOfCreatedDraft);
        logRequest.setPatientId(request.getPatientPersonnummer());
        logRequest.setPatientName(request.getPatientFornamn(), request.getPatientEfternamn());
        
        logService.logCreateOfIntyg(logRequest);
    }

    private CreateNewDraftRequest createServiceRequest(CreateNewIntygRequest req) {

        CreateNewDraftRequest srvReq = new CreateNewDraftRequest();

        srvReq.setIntygType(req.getIntygType());

        Patient pat = new Patient();
        pat.setPersonNummer(req.getPatientPersonnummer());
        pat.setForNamn(req.getPatientFornamn());
        pat.setEfterNamn(req.getPatientEfternamn());
        srvReq.setPatient(pat);

        HoSPerson hosp = createHoSPersonFromUser();
        srvReq.setHosPerson(hosp);

        Vardgivare vgiv = new Vardgivare();
        vgiv.setHsaId(req.getVardGivareHsaId());
        vgiv.setNamn(req.getVardGivareNamn());

        Vardenhet venh = new Vardenhet();
        venh.setVardgivare(vgiv);
        venh.setHsaId(req.getVardEnhetHsaId());
        venh.setNamn(req.getVardEnhetNamn());

        srvReq.setVardenhet(venh);

        return srvReq;
    }

    /**
     * Compiles a list of Intyg from two data sources. Signed Intyg are
     * retrieved from Intygstjänst, drafts are retrieved from Webcerts db. Both
     * types of Intyg are converted and merged into one sorted list.
     * 
     * @param personNummer
     * @return a Response carrying a list containing all Intyg for a person.
     */
    @GET
    @Path("/list/{personNummer}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response listIntyg(@PathParam("personNummer") String personNummer) {

        LOG.debug("Retrieving intyg for person {}", personNummer);

        List<String> enhetsIds = getEnhetIdsForCurrentUser();

        if (enhetsIds.isEmpty()) {
            LOG.error("Current user has no assignments");
            return Response.status(Status.BAD_REQUEST).build();
        }

        List<IntygItem> signedIntygList = intygService.listIntyg(enhetsIds, personNummer);
        LOG.debug("Got {} signed intyg", signedIntygList.size());

        List<Intyg> draftIntygList = intygRepository.findDraftsByPatientAndEnhetAndStatus(personNummer, enhetsIds,
                ALL_DRAFTS);
        LOG.debug("Got {} draft intyg", draftIntygList.size());

        List<ListIntygEntry> allIntyg = IntygDraftsConverter.merge(signedIntygList, draftIntygList);

        return Response.ok(allIntyg).build();
    }

    @GET
    @Path("/unsigned")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getUnsignedIntygForUnit() {
        
        WebCertUser user = webCertUserService.getWebCertUser();
        String selectedUnitHsaId = user.getValdVardenhet().getId();
        
        IntygFilter filter = new IntygFilter(selectedUnitHsaId);
        filter.setStatusList(ALL_DRAFTS);
        filter.setPageSize(10);
        filter.setStartFrom(0);
        
        QueryIntygResponse response = performIntygFilterQuery(selectedUnitHsaId, filter);
                
        return Response.ok(response).build();
    }

    @PUT
    @Path("/unsigned")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response filterUnsignedIntygForUnit(QueryIntygParameter filterDto) {
        
        WebCertUser user = webCertUserService.getWebCertUser();
        String selectedUnitHsaId = user.getValdVardenhet().getId();
        
        IntygFilter filter = new IntygFilter(selectedUnitHsaId);
        
        if (Boolean.FALSE.equals(filterDto.getComplete())) {
            filter.setStatusList(INCOMPLETE_DRAFTS);
        } else if (Boolean.TRUE.equals(filterDto.getComplete())) {
            filter.setStatusList(COMPLETE_DRAFTS);
        } else {
            filter.setStatusList(ALL_DRAFTS);
        }
        
        filter.setSavedFrom(filterDto.getSavedFrom());
        filter.setSavedTo(filterDto.getSavedTo());
        filter.setSavedByHsaId(filterDto.getSavedBy());
        filter.setForwarded(filterDto.getForwarded());
        filter.setPageSize(filterDto.getPageSize());
        filter.setStartFrom(filterDto.getStartFrom());
        
        QueryIntygResponse queryResponse = performIntygFilterQuery(selectedUnitHsaId, filter);
                        
        return Response.ok(queryResponse).build();
    }
    
    private QueryIntygResponse performIntygFilterQuery(String selectedUnitHsaId, IntygFilter filter) {
        
        List<Intyg> intygList = intygRepository.filterIntyg(filter);
                
        List<ListIntygEntry> listIntygEntries = IntygDraftsConverter.convertIntygToListEntries(intygList);
        
        int totalCountOfFilteredIntyg = intygRepository.countFilterIntyg(filter);
        
        QueryIntygResponse response = new QueryIntygResponse(listIntygEntries);
        response.setTotalCount(totalCountOfFilteredIntyg);
        return response;
    }

    /**
     * Returns a list of all deployed modules.
     * 
     * @return
     */
    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response listIntygTypes() {

        List<IntygModule> allModules = moduleRegistry.listAllModules();

        LOG.debug("Returning list of {} modules", allModules.size());

        return Response.ok().entity(allModules).build();
    }
    
    /**
     * Returns a list of doctors that have one or more unsigned intyg.
     * 
     * @return a list of {@link se.inera.webcert.service.dto.Lakare} objects.
     */
    @GET
    @Path("/unsigned/lakare")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getIntygLakareByEnhet() {
        
        WebCertUser user = webCertUserService.getWebCertUser();
        String selectedUnitHsaId = user.getValdVardenhet().getId();
        
        List<Lakare> lakareWithDraftsByEnhet = intygDraftService.getLakareWithDraftsByEnhet(selectedUnitHsaId);
                
        return Response.ok().entity(lakareWithDraftsByEnhet).build();
    }
}
