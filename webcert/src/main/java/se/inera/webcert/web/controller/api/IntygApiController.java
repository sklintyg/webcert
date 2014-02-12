package se.inera.webcert.web.controller.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.converter.IntygMerger;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.modules.ModuleRestApiFactory;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.IntygService;
import se.inera.webcert.service.draft.IntygDraftService;
import se.inera.webcert.service.dto.IntygItem;
import se.inera.webcert.web.controller.api.dto.CreateNewIntygRequest;
import se.inera.webcert.web.controller.api.dto.ListIntygEntry;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * Controller for the API that serves WebCert.
 * 
 * @author nikpet
 *
 */
public class IntygApiController {
    
    private static final String UTF_8 = ";charset=utf-8";
    
    private static Logger LOG = LoggerFactory.getLogger(IntygApiController.class);
    
    @Autowired
    private WebCertUserService webCertUserService;
    
    @Autowired
    private IntygService intygService;
    
    @Autowired
    private IntygRepository intygRepository;
    
    @Autowired
    private ModuleRestApiFactory moduleApiFactory;
    
    @Autowired
    private IntygDraftService intygDraftService;
            
    public IntygApiController() {
        
    }
    
    @POST
    @Path("/create")
    public Response createNewIntyg(CreateNewIntygRequest request) {
        
        intygDraftService.createNewDraft(request.getIntygType(), request.getPatientPersonnummer());
        
        // TODO: return a redirect to the edit page of the module with the newly generated draft.
        
        return null;
    }
    
    /**
     * Compiles a list of Intyg from two data sources. Signed Intyg are retrieved from Intygstjänst,
     * drafts are retrieved from Webcerts db. Both types of Intyg are converted and merged into one
     * sorted list.
     * 
     * @param personNummer
     * @return a Response carrying a list containing all Intyg for a person.
     */
    @GET
    @Path("/list/{personNummer}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8)
    public Response listIntyg(@PathParam("personNummer") String personNummer) {
        
        LOG.debug("Retrieving intyg for person {}", personNummer);
        
        List<String> enhetIds = getEnhetIdsForCurrentUser();
        
        if (enhetIds.isEmpty()) {
            LOG.error("Current user has no assignments");
            return Response.status(Status.BAD_REQUEST).build();
        }
        
        List<IntygItem> signedIntygList = intygService.listIntyg(enhetIds, personNummer);
        
        List<Intyg> draftIntygList = intygRepository.findDraftsByPatientPnrAndEnhetsId(enhetIds, personNummer);
        
        List<ListIntygEntry> allIntyg = IntygMerger.merge(signedIntygList, draftIntygList);
        
        return Response.ok(allIntyg).build();
    }
    
    private List<String> getEnhetIdsForCurrentUser() {
        
        WebCertUser webCertUser = webCertUserService.getWebCertUser();
        
        return webCertUser.getVardenheterIds();
    }
    
}
