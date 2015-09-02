package se.inera.webcert.web.controller.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.OptimisticLockException;
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
import org.springframework.dao.OptimisticLockingFailureException;

import se.inera.webcert.converter.IntygDraftsConverter;
import se.inera.webcert.service.dto.Vardenhet;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.feature.WebcertFeature;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.intyg.dto.IntygItemListResponse;
import se.inera.webcert.service.monitoring.MonitoringLogService;
import se.inera.webcert.service.utkast.CopyUtkastService;
import se.inera.webcert.service.utkast.UtkastService;
import se.inera.webcert.service.utkast.dto.CreateNewDraftCopyRequest;
import se.inera.webcert.service.utkast.dto.CreateNewDraftCopyResponse;
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

    private static final List<UtkastStatus> ALL_DRAFTS = Arrays.asList(UtkastStatus.DRAFT_COMPLETE,
            UtkastStatus.DRAFT_INCOMPLETE);

    // TODO Where to put this stuff?
    private static final String OFFLINE_MODE = "offline_mode";

    @Autowired
    private IntygService intygService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private UtkastService utkastService;

    @Autowired
    private CopyUtkastService copyUtkastService;

    @Autowired
    private MonitoringLogService monitoringLogService;

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

        if (!request.isValid()) {
            LOG.error("Request to create copy of '{}' is not valid", orgIntygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Missing vital arguments in payload");
        }

        CreateNewDraftCopyRequest serviceRequest = createNewDraftCopyRequest(orgIntygsId, intygsTyp, request);

        CreateNewDraftCopyResponse serviceResponse = copyUtkastService.createCopy(serviceRequest);

        LOG.debug("Created a new draft copy from '{}' with id '{}' and type {}", new Object[] { orgIntygsId, serviceResponse.getNewDraftIntygId(),
                serviceResponse.getNewDraftIntygType() });

        CopyIntygResponse response = new CopyIntygResponse(serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType());

        return Response.ok().entity(response).build();
    }

    private CreateNewDraftCopyRequest createNewDraftCopyRequest(String originalIntygId, String intygsTyp, CopyIntygRequest copyRequest) {

        HoSPerson hosPerson = createHoSPersonFromUser();
        Vardenhet vardenhet = createVardenhetFromUser();
        String patientPersonnummer = copyRequest.getPatientPersonnummer();

        CreateNewDraftCopyRequest req = new CreateNewDraftCopyRequest(originalIntygId, intygsTyp, patientPersonnummer, hosPerson, vardenhet);

        if (copyRequest.containsNewPersonnummer()) {
            LOG.debug("Adding new personnummer to request");
            req.setNyttPatientPersonnummer(copyRequest.getNyttPatientPersonnummer());
        }

        if (checkIfWebcertFeatureIsAvailable(WebcertFeature.FRAN_JOURNALSYSTEM)) {
            LOG.debug("Setting djupintegrerad flag on request to true");
            req.setDjupintegrerad(true);
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

        IntygItemListResponse intygItemListResponse = intygService.listIntyg(enhetsIds, personNummer);
        LOG.debug("Got {} intyg", intygItemListResponse.getIntygItemList().size());

        List<Utkast> utkastList;

        if (checkIfWebcertFeatureIsAvailable(WebcertFeature.HANTERA_INTYGSUTKAST)) {
            utkastList = utkastRepository.findDraftsByPatientAndEnhetAndStatus(personNummer, enhetsIds,
                    ALL_DRAFTS);
            LOG.debug("Got {} utkast", utkastList.size());
        } else {
            utkastList = Collections.emptyList();
        }

        List<ListIntygEntry> allIntyg = IntygDraftsConverter.merge(intygItemListResponse.getIntygItemList(), utkastList);

        Response.ResponseBuilder responseBuilder = Response.ok(allIntyg);
        if (intygItemListResponse.isOfflineMode()) {
            responseBuilder = responseBuilder.header(OFFLINE_MODE, Boolean.TRUE.toString());
        }
        return responseBuilder.build();
    }

    /**
     * Sets the notified flag on an Intyg.
     *
     * @param intygsId
     *            Id of the Intyg
     * @param notified
     *            True or False
     * @return
     *         Response
     */
    @PUT
    @Path("/{intygsTyp}/{intygsId}/{version}/vidarebefordra")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setNotifiedOnIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
                                       @PathParam("version") long version, Boolean notified) {

        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);

        Utkast updatedIntyg;
        try {
            updatedIntyg = utkastService.setNotifiedOnDraft(intygsId, version, notified);
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(intygsId, intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }

        LOG.debug("Set forward to {} on intyg {} with id '{}'",
                new Object[] { updatedIntyg.getVidarebefordrad(), intygsTyp, updatedIntyg.getIntygsId() });

        ListIntygEntry intygEntry = IntygDraftsConverter.convertUtkastToListIntygEntry(updatedIntyg);

        return Response.ok(intygEntry).build();
    }
}
