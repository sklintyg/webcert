package se.inera.webcert.web.controller.api;

import java.util.Arrays;
import java.util.Collections;
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
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.draft.IntygDraftService;
import se.inera.webcert.service.draft.dto.CreateNewDraftCopyRequest;
import se.inera.webcert.service.draft.dto.CreateNewDraftCopyResponse;
import se.inera.webcert.service.feature.WebcertFeature;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.web.controller.AbstractApiController;
import se.inera.webcert.web.controller.api.dto.CopyIntygRequest;
import se.inera.webcert.web.controller.api.dto.CopyIntygResponse;
import se.inera.webcert.web.controller.api.dto.ListIntygEntry;

/**
 * Controller for the API that serves WebCert.
 *
 * @author nikpet
 *
 */
@Path("/intyg")
public class IntygApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(IntygApiController.class);

    private static final List<IntygsStatus> ALL_DRAFTS = Arrays.asList(IntygsStatus.DRAFT_COMPLETE,
            IntygsStatus.DRAFT_INCOMPLETE);

    @Autowired
    private IntygService intygService;

    @Autowired
    private IntygRepository intygRepository;

    @Autowired
    private IntygDraftService intygDraftService;

    public IntygApiController() {

    }

    /**
     * Create a copy of a certificate.
     * 
     * @param request
     * @param intygsTyp
     * @param orgIntygsId
     * @return
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/kopiera")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response createNewCopy(CopyIntygRequest request, @PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String orgIntygsId) {

        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.KOPIERA_INTYG, intygsTyp);

        LOG.debug("Attempting to create a draft copy of {} with id '{}'", intygsTyp, orgIntygsId);

        CreateNewDraftCopyRequest serviceRequest = createNewDraftCopyRequest(orgIntygsId, request);

        CreateNewDraftCopyResponse serviceResponse = intygDraftService.createNewDraftCopy(serviceRequest);

        LOG.debug("Created a new draft copy from '{}' with id '{}' and type {}", new Object[] { orgIntygsId, serviceResponse.getNewDraftIntygId(),
                serviceResponse.getNewDraftIntygType() });

        CopyIntygResponse response = new CopyIntygResponse(serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType());

        return Response.ok().entity(response).build();
    }

    private CreateNewDraftCopyRequest createNewDraftCopyRequest(String originalIntygId, CopyIntygRequest copyRequest) {

        CreateNewDraftCopyRequest req = new CreateNewDraftCopyRequest();
        req.setOriginalIntygId(originalIntygId);

        req.setHosPerson(createHoSPersonFromUser());
        req.setVardenhet(createVardenhetFromUser());

        if (copyRequest != null && copyRequest.containsNewPersonnummer()) {
            req.setNyttPatientPersonnummer(copyRequest.getNyttPatientPersonnummer());
        }

        return req;
    }

    /**
     * Compiles a list of Intyg from two data sources. Signed Intyg are
     * retrieved from Intygstjänst, drafts are retrieved from Webcerts db. Both
     * types of Intyg are converted and merged into one sorted list.
     *
     * @param personNummer
     *            personnummer
     * @return a Response carrying a list containing all Intyg for a person.
     */
    @GET
    @Path("/person/{personNummer}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response listDraftsAndIntygForPerson(@PathParam("personNummer") String personNummer) {

        LOG.debug("Retrieving intyg for person {}", personNummer);

        List<String> enhetsIds = getEnhetIdsForCurrentUser();

        if (enhetsIds.isEmpty()) {
            LOG.error("Current user has no assignments");
            return Response.status(Status.BAD_REQUEST).build();
        }

        List<IntygItem> signedIntygList = intygService.listIntyg(enhetsIds, personNummer);
        LOG.debug("Got {} signed intyg", signedIntygList.size());

        List<Intyg> draftIntygList;

        if (checkIfWebcertFeatureIsAvailable(WebcertFeature.HANTERA_INTYGSUTKAST)) {
            draftIntygList = intygRepository.findDraftsByPatientAndEnhetAndStatus(personNummer, enhetsIds,
                    ALL_DRAFTS);
            LOG.debug("Got {} draft intyg", draftIntygList.size());
        } else {
            draftIntygList = Collections.emptyList();
        }

        List<ListIntygEntry> allIntyg = IntygDraftsConverter.merge(signedIntygList, draftIntygList);

        return Response.ok(allIntyg).build();
    }

    /**
     * Sets the forwarded flag on an Intyg.
     *
     * @param intygsId
     *            Id of the Intyg
     * @param forwarded
     *            True or False
     * @return
     *         Response
     */
    @PUT
    @Path("/{intygsTyp}/{intygsId}/vidarebefordra")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setForwardOnIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId, Boolean forwarded) {

        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);

        Intyg updatedIntyg = intygDraftService.setForwardOnDraft(intygsId, forwarded);

        LOG.debug("Set forward to {} on intyg {} with id '{}'",
                new Object[] { updatedIntyg.getVidarebefordrad(), intygsTyp, updatedIntyg.getIntygsId() });

        ListIntygEntry intygEntry = IntygDraftsConverter.convertIntygToListIntygEntry(updatedIntyg);

        return Response.ok(intygEntry).build();
    }
}
