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

import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.feature.WebcertFeature;
import se.inera.webcert.service.log.LogRequestFactory;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.service.signatur.SignaturService;
import se.inera.webcert.service.signatur.dto.SignaturTicket;
import se.inera.webcert.service.utkast.UtkastService;
import se.inera.webcert.service.utkast.dto.DraftValidation;
import se.inera.webcert.service.utkast.dto.DraftValidationMessage;
import se.inera.webcert.service.utkast.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.web.controller.AbstractApiController;
import se.inera.webcert.web.controller.moduleapi.dto.SignaturTicketResponse;
import se.inera.webcert.web.controller.moduleapi.dto.DraftValidationStatus;
import se.inera.webcert.web.controller.moduleapi.dto.DraftHolder;
import se.inera.webcert.web.controller.moduleapi.dto.SaveDraftResponse;
import se.inera.webcert.web.service.WebCertUserService;

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
    private UtkastService utkastService;

    @Autowired
    private SignaturService signaturService;

    @Autowired
    private LogService logService;

    @Autowired
    private WebCertUserService webCertUserService;

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

        Utkast utkast = utkastService.getDraft(intygsId);
        
        abortIfUserNotAuthorizedForUnit(utkast.getVardgivarId(), utkast.getEnhetsId());

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        logService.logReadOfIntyg(logRequest, webCertUserService.getWebCertUser());

        DraftHolder draftHolder = new DraftHolder();

        draftHolder.setVidarebefordrad(utkast.getVidarebefordrad());
        draftHolder.setStatus(utkast.getStatus());
        draftHolder.setContent(utkast.getModel());

        return Response.ok(draftHolder).build();
    }

    /**
     * Persists the supplied draft certificate using the intygId as key.
     *
     * @param intygsId
     *            The id of the certificate.
     * @param payload
     *            Object holding the certificate and its current status.
     */
    @PUT
    @Path("/{intygsTyp}/{intygsId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response saveDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId, @DefaultValue("false") @QueryParam("autoSave") boolean autoSave, byte[] payload) {

        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);

        LOG.debug("Saving utkast with id '{}', autosave is {}", intygsId, autoSave);

        String draftAsJson = fromBytesToString(payload);

        SaveAndValidateDraftRequest serviceRequest = createSaveAndValidateDraftRequest(intygsId, draftAsJson, autoSave);
        DraftValidation draftValidation = utkastService.saveAndValidateDraft(serviceRequest);

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
            responseEntity.addMessage(validationMessage.getField(), validationMessage.getType(), validationMessage.getMessage());
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

        utkastService.deleteUnsignedDraft(intygsId);

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

        Utkast utkast = utkastService.getDraft(intygsId);

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);

        logService.logPrintOfIntygAsDraft(logRequest, webCertUserService.getWebCertUser());

        return Response.ok().build();
    }

    /**
     * Signera utkast.
     *
     * @param intygsId
     *            intyg id
     * @return SignaturTicketResponse
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/signeraserver")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public SignaturTicketResponse serverSigneraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId) {
        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);
        SignaturTicket ticket = utkastService.serverSignature(intygsId);
        return new SignaturTicketResponse(ticket);
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
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM})
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public SignaturTicketResponse klientSigneraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("biljettId") String biljettId, byte[] rawSignatur) {
        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);
        LOG.debug("Signerar intyg med biljettId {}", biljettId);

        if (rawSignatur.length == 0) {
            LOG.error("Inkommande signatur parameter saknas");
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Signatur saknas");
        }

        String rawSignaturString = fromBytesToString(rawSignatur);
        SignaturTicket ticket = signaturService.clientSignature(biljettId, rawSignaturString);
        return new SignaturTicketResponse(ticket);
    }

    /**
     * Skapa signeringshash.
     *
     * @param intygsId
     *            intyg id
     * @return SignaturTicketResponse
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/signeringshash")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public SignaturTicketResponse signeraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId) {
        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);
        SignaturTicket ticket = utkastService.createDraftHash(intygsId);
        return new SignaturTicketResponse(ticket);
    }

    /**
     * Hamta signeringsstatus.
     *
     * @param biljettId
     *            biljett id
     * @return SignaturTicketResponse
     */
    @GET
    @Path("/{intygsTyp}/{biljettId}/signeringsstatus")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public SignaturTicketResponse biljettStatus(@PathParam("intygsTyp") String intygsTyp, @PathParam("biljettId") String biljettId) {
        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.HANTERA_INTYGSUTKAST, intygsTyp);
        SignaturTicket ticket = signaturService.ticketStatus(biljettId);
        return new SignaturTicketResponse(ticket);
    }
}
