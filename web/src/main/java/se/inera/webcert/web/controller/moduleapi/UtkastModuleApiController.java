package se.inera.webcert.web.controller.moduleapi;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.service.draft.IntygDraftService;
import se.inera.webcert.service.draft.IntygSignatureService;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.dto.DraftValidationMessage;
import se.inera.webcert.service.draft.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.draft.dto.SignatureTicket;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.feature.WebcertFeature;
import se.inera.webcert.service.log.LogRequestFactory;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.web.controller.AbstractApiController;
import se.inera.webcert.web.controller.moduleapi.dto.BiljettResponse;
import se.inera.webcert.web.controller.moduleapi.dto.DraftValidationStatus;
import se.inera.webcert.web.controller.moduleapi.dto.IntygDraftHolder;
import se.inera.webcert.web.controller.moduleapi.dto.SaveDraftResponse;

/**
 * Controller for module interaction with drafts.
 *
 * @author npet
 *
 */
@Path("/utkast")
public class UtkastModuleApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(UtkastModuleApiController.class);

    @Autowired
    private IntygDraftService draftService;

    @Autowired
    private IntygSignatureService signatureService;

    @Autowired
    private LogService logService;

    /**
     * Returns the draft certificate as JSON identified by the intygId.
     *
     * @param intygsId
     *            The id of the certificate
     * @return a JSON object
     */
    @GET
    @Path("/{intygsTyp}/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId) {

        LOG.debug("Retrieving Intyg with id {} and type {}", intygsId, intygsTyp);

        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);

        Intyg intyg = draftService.getDraft(intygsId);
        
        LogRequest logRequest = LogRequestFactory.createLogRequestFromDraft(intyg);
        logService.logReadOfIntyg(logRequest);

        IntygDraftHolder draftHolder = new IntygDraftHolder();

        draftHolder.setVidarebefordrad(intyg.getVidarebefordrad());
        draftHolder.setStatus(intyg.getStatus());
        draftHolder.setContent(intyg.getModel());

        return Response.ok(draftHolder).build();
    }

    /**
     * Persists the supplied draft certificate using the intygId as key.
     *
     * @param intygsId
     *            The id of the certificate.
     * @param draftCertificate
     *            Object holding the certificate and its current status.
     */
    @PUT
    @Path("/{intygsTyp}/{intygsId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response saveDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId, @DefaultValue("false") @QueryParam("autoSave") boolean autoSave,  byte[] draftCertificate) {

        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);

        LOG.debug("Saving Intyg with id {}", intygsId);
        LOG.debug("Autosave is {}", autoSave);

        String draftAsJson = fromBytesToString(draftCertificate);

        SaveAndValidateDraftRequest serviceRequest = createSaveAndValidateDraftRequest(intygsId, draftAsJson, autoSave);
        DraftValidation draftValidation = draftService.saveAndValidateDraft(serviceRequest);

        SaveDraftResponse responseEntity = buildSaveDraftResponse(draftValidation);

        return Response.ok().entity(responseEntity).build();
    }

    private SaveAndValidateDraftRequest createSaveAndValidateDraftRequest(String intygId, String draftAsJson, Boolean autoSave) {
        SaveAndValidateDraftRequest request = new SaveAndValidateDraftRequest();

        request.setIntygId(intygId);
        request.setDraftAsJson(draftAsJson);
        request.setAutoSave(autoSave);

        HoSPerson savedBy = createHoSPersonFromUser();
        request.setSavedBy(savedBy);

        return request;
    }

    private SaveDraftResponse buildSaveDraftResponse(DraftValidation draftValidation) {

        if (draftValidation.isDraftValid()) {
            return new SaveDraftResponse(DraftValidationStatus.COMPLETE);
        }

        SaveDraftResponse responseEntity = new SaveDraftResponse(DraftValidationStatus.INCOMPLETE);

        List<DraftValidationMessage> validationMessages = draftValidation.getMessages();

        for (DraftValidationMessage validationMessage : validationMessages) {
            responseEntity.addMessage(validationMessage.getField(), validationMessage.getMessage());
        }

        return responseEntity;
    }

    private String fromBytesToString(byte[] bytes) {
        try {
            return new String(bytes, UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not convert the payload from draftCertificate to String!", e);
        }
    }

    /**
     * Deletes a draft certificate identified by the certificateId.
     *
     * @param intygsId
     *            The id of the certificate
     */
    @DELETE
    @Path("/{intygsTyp}/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response discardDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId) {

        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);

        LOG.debug("Deleting draft with id {}", intygsId);

        draftService.deleteUnsignedDraft(intygsId);

        return Response.ok().build();
    }

    /**
     * Creates a PDL log event that a persons draft has been printed.
     *
     * @param intygsId
     * @return
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/loggautskrift")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response logPrintOfDraftToPDL(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId) {

        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);

        LOG.debug("Logging printout of draft intyg '{}'", intygsId);

        Intyg draft = draftService.getDraft(intygsId);

        LogRequest logRequest = LogRequestFactory.createLogRequestFromDraft(draft);

        logService.logPrintOfIntygAsDraft(logRequest);

        return Response.ok().build();
    }

    /**
     * Signera utkast.
     *
     * @param intygsId
     *            intyg id
     * @return BiljettResponse
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/signeraserver")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public BiljettResponse serverSigneraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId) {
        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);
        SignatureTicket biljett = signatureService.serverSignature(intygsId);
        return new BiljettResponse(biljett);
    }

    /**
     * Signera utkast.
     *
     * @param biljettId
     *            biljett id
     * @return BiljettResponse
     */
    @POST
    @Path("/{intygsTyp}/{biljettId}/signeraklient")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public BiljettResponse klientSigneraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("biljettId") String biljettId, byte[] rawSignatur) {
        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);
        LOG.debug("Signerar intyg med biljettId {}", biljettId);
        String draftAsJson = fromBytesToString(rawSignatur);
        SignatureTicket biljett = signatureService.clientSignature(biljettId, draftAsJson);
        return new BiljettResponse(biljett);
    }

    /**
     * Skapa signeringshash.
     *
     * @param intygsId
     *            intyg id
     * @return BiljettResponse
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/signeringshash")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public BiljettResponse signeraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId) {
        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);
        SignatureTicket biljett = signatureService.createDraftHash(intygsId);
        return new BiljettResponse(biljett);
    }

    /**
     * Hamta signeringsstatus.
     *
     * @param biljettId
     *            biljett id
     * @return BiljettResponse
     */
    @GET
    @Path("/{intygsTyp}/{biljettId}/signeringsstatus")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public BiljettResponse biljettStatus(@PathParam("intygsTyp") String intygsTyp, @PathParam("biljettId") String biljettId) {
        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);
        SignatureTicket biljett = signatureService.ticketStatus(biljettId);
        return new BiljettResponse(biljett);
    }
}
