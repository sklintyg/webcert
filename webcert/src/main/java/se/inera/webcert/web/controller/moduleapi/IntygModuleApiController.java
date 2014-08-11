package se.inera.webcert.web.controller.moduleapi;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.service.draft.IntygDraftService;
import se.inera.webcert.service.draft.IntygSignatureService;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.dto.DraftValidationMessage;
import se.inera.webcert.service.draft.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.draft.dto.SignatureTicket;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.intyg.dto.IntygContentHolder;
import se.inera.webcert.service.intyg.dto.IntygPdf;
import se.inera.webcert.service.log.LogRequestFactory;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.web.controller.AbstractApiController;
import se.inera.webcert.web.controller.moduleapi.dto.BiljettResponse;
import se.inera.webcert.web.controller.moduleapi.dto.DraftValidationStatus;
import se.inera.webcert.web.controller.moduleapi.dto.IntygDraftHolder;
import se.inera.webcert.web.controller.moduleapi.dto.SaveDraftResponse;
import se.inera.webcert.web.controller.moduleapi.dto.SendSignedIntygParameter;

/**
 * Controller exposing services to be used by modules.
 *
 * @author nikpet
 */
public class IntygModuleApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(IntygModuleApiController.class);

    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    @Autowired
    private IntygService intygService;

    @Autowired
    private IntygDraftService draftService;

    @Autowired
    private IntygSignatureService signatureService;
    
    @Autowired
    private LogService logService;

    /**
     * Returns the draft certificate as JSON identified by the intygId.
     *
     * @param intygId The id of the certificate
     * @return a JSON object
     */
    @GET
    @Path("/draft/{intygId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getDraft(@PathParam("intygId") String intygId) {

        LOG.debug("Retrieving Intyg with id {}", intygId);

        Intyg intyg = draftService.getDraft(intygId);

        IntygDraftHolder draftHolder = new IntygDraftHolder();

        draftHolder.setStatus(intyg.getStatus());
        draftHolder.setContent(intyg.getModel());

        return Response.ok(draftHolder).build();
    }

    /**
     * Persists the supplied draft certificate using the intygId as key.
     *
     * @param intygId          The id of the certificate.
     * @param draftCertificate Object holding the certificate and its current status.
     */
    @PUT
    @Path("/draft/{intygId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Transactional
    public Response saveDraft(@PathParam("intygId") String intygId, byte[] draftCertificate) {

        LOG.debug("Saving Intyg with id {}", intygId);

        String draftAsJson = fromBytesToString(draftCertificate);

        SaveAndValidateDraftRequest serviceRequest = createSaveAndValidateDraftRequest(intygId, draftAsJson);
        DraftValidation draftValidation = draftService.saveAndValidateDraft(serviceRequest);

        SaveDraftResponse responseEntity = buildSaveDraftResponse(draftValidation);

        return Response.ok().entity(responseEntity).build();
    }

    private SaveAndValidateDraftRequest createSaveAndValidateDraftRequest(String intygId, String draftAsJson) {
        SaveAndValidateDraftRequest request = new SaveAndValidateDraftRequest();

        request.setIntygId(intygId);
        request.setDraftAsJson(draftAsJson);

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
     * Marks the draft certificate identified by the certificateId as discarded.
     *
     * @param intygId The id of the certificate
     */
    @DELETE
    @Path("/draft/{intygId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Transactional
    public Response discardDraft(@PathParam("intygId") String intygId) {

        LOG.debug("Deleting draft with id {}", intygId);

        draftService.deleteUnsignedDraft(intygId);

        return Response.ok().build();
    }

    /**
     * Delivers a signed intyg.
     *
     * @param intygId intygid
     * @return Response
     */
    @GET
    @Path("/signed/{intygId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getSignedIntyg(@PathParam("intygId") String intygId) {

        LOG.debug("Fetching signed intyg with id '{}' from IT", intygId);

        IntygContentHolder intygAsExternal = intygService.fetchIntygData(intygId);

        return Response.ok().entity(intygAsExternal).build();
    }

    /**
     * Return the signed certificate identified by the given id as PDF.
     *
     * @param intygId - the globally unique id of a certificate.
     * @return The certificate in PDF format
     */
    @GET
    @Path("/signed/{intygId}/pdf")
    @Produces("application/pdf")
    public final Response getSignedIntygAsPdf(@PathParam(value = "intygId") final String intygId) {

        LOG.debug("Fetching signed intyg '{}' as PDF", intygId);

        IntygPdf intygPdfResponse = intygService.fetchIntygAsPdf(intygId);

        return Response.ok(intygPdfResponse.getPdfData()).header(CONTENT_DISPOSITION, buildPdfHeader(intygPdfResponse.getFilename())).build();
    }

    private String buildPdfHeader(String pdfFileName) {
        return "attachment; filename=\"" + pdfFileName + "\"";
    }

    /**
     * Creates a PDL log event that a persons draft has been printed.
     * 
     * @param intygId
     * @return
     */
    @POST
    @Path("/draft/logprint")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response logPrintOfDraft(String intygId) {
        
        LOG.debug("Logging printout of draft intyg '{}'", intygId);
        
        IntygContentHolder externalIntygData = intygService.fetchExternalIntygData(intygId);
        
        LogRequest logRequest = LogRequestFactory.createLogRequestFromExternalModel(externalIntygData.getExternalModel());
        logService.logPrintOfIntygAsDraft(logRequest);
        
        return Response.ok().build();
    }
    
    @POST
    @Path("/signed/{intygId}/send")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response sendSignedIntyg(@PathParam("intygId") String intygId, SendSignedIntygParameter param) {
        boolean sendSuccess = intygService.sendIntyg(intygId, param.getRecipient(), param.isPatientConsent());
        
        return (sendSuccess) ? Response.ok().build() : Response.serverError().build();
    }
    
    /**
     * Signera utkast.
     *
     * @param intygsId intyg id
     * @return BiljettResponse
     */
    @POST
    @Path("/signera/server/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public BiljettResponse serverSigneraUtkast(@PathParam("intygsId") String intygsId) {
        SignatureTicket biljett = draftService.serverSignature(intygsId);
        return new BiljettResponse(biljett);
    }

    /**
     * Signera utkast.
     *
     * @param biljettId biljett id
     * @return BiljettResponse
     */
    @POST
    @Path("/signera/klient/{biljettId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public BiljettResponse klientSigneraUtkast(@PathParam("biljettId") String biljettId, byte[] rawSignatur) {
        LOG.debug("Signerar intyg med biljettId {}", biljettId);

        String draftAsJson = fromBytesToString(rawSignatur);

        SignatureTicket biljett = signatureService.clientSignature(biljettId, draftAsJson);
        return new BiljettResponse(biljett);
    }

    /**
     * Skapa signeringshash.
     *
     * @param intygsId intyg id
     * @return BiljettResponse
     */
    @POST
    @Path("/signeringshash/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public BiljettResponse signeraUtkast(@PathParam("intygsId") String intygsId) {
        SignatureTicket biljett = draftService.createDraftHash(intygsId);
        return new BiljettResponse(biljett);
    }

    /**
     * Hamta signeringsstatus.
     *
     * @param biljettId biljett id
     * @return BiljettResponse
     */
    @GET
    @Path("/signeringsstatus/{biljettId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public BiljettResponse biljettStatus(@PathParam("biljettId") String biljettId) {
        SignatureTicket biljett = signatureService.ticketStatus(biljettId);
        return new BiljettResponse(biljett);
    }

}
