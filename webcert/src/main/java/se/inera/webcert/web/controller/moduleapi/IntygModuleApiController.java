package se.inera.webcert.web.controller.moduleapi;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import org.springframework.transaction.annotation.Transactional;

import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.ExternalModelHolder;
import se.inera.certificate.modules.support.api.dto.PdfResponse;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.modules.IntygModuleRegistry;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.IntygService;
import se.inera.webcert.service.draft.IntygDraftService;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.dto.DraftValidationMessage;
import se.inera.webcert.service.draft.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.dto.IntygContentHolder;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.web.controller.AbstractApiController;
import se.inera.webcert.web.controller.moduleapi.dto.DraftValidationStatus;
import se.inera.webcert.web.controller.moduleapi.dto.IntygDraftHolder;
import se.inera.webcert.web.controller.moduleapi.dto.SaveDraftResponse;

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
    private IntygRepository intygRepository;
        
    @Autowired
    private IntygModuleRegistry moduleRegistry;
    
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
        
        Intyg intyg = intygRepository.findOne(intygId);
        
        if (intyg == null) {
            LOG.warn("Intyg with id {} was not found", intygId);
            return Response.status(Status.NOT_FOUND).build();
        }
        
        IntygDraftHolder draftHolder = new IntygDraftHolder();
        
        draftHolder.setStatus(intyg.getStatus());
        draftHolder.setContent(intyg.getModel());
        
        return Response.ok(draftHolder).build();
    }
    
    /**
     * Persists the supplied draft certificate using the intygId as key.
     * 
     * @param intygId
     *            The id of the certificate.
     * @param draftCertificate Object holding the certificate and its current status.
     */
    @PUT
    @Path("/draft/{intygId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Transactional
    public Response saveDraft(@PathParam("intygId") String intygId, byte[] bytes) {
                        
        LOG.debug("Saving Intyg with id {}", intygId);
        
        String draftAsJson = fromBytesToString(bytes);
        
        SaveAndValidateDraftRequest serviceRequest = createSaveAndValidateDraftRequest(intygId, draftAsJson);
        DraftValidation draftValidation = draftService.saveAndValidateDraft(serviceRequest);
                
        if (draftValidation == null) {
            LOG.warn("Intyg with id {} was not found", intygId);
            return Response.status(Status.NOT_FOUND).build();
        }
                
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
            throw new RuntimeException("Could not convert the payload from bytes to String!", e);
        }
    }
    
    /**
     * Marks the draft certificate identified by the certificateId as discarded.
     * 
     * @param intygId The id of the certificate
     */
    @DELETE
    @Path("/draft/{intygId}")
    @Transactional
    public Response discardDraft(@PathParam("intygId") String intygId) {
        
        LOG.debug("Deleting draft with id {}", intygId);
        
        draftService.deleteUnsignedDraft(intygId);
                
        return Response.ok().build();
    }
    
    /**
     * Delivers a signed intyg
     * 
     * @param intygId
     * @return
     */
    @GET
    @Path("/signed/{intygId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getSignedIntyg(@PathParam("intygId") String intygId) {
        
        LOG.debug("Fetching signed intyg with id '{}' from IT", intygId);
        
        IntygContentHolder intygAsExternal = intygService.fetchIntygData(intygId);
        String patientId = intygAsExternal.getMetaData().getPatientId();
        
        logService.logReadOfIntyg(intygId, patientId);
        
        return Response.ok().entity(intygAsExternal).build();
    }

    /**
     * Return the signed certificate identified by the given id as PDF.
     * 
     * @param id
     *            - the globally unique id of a certificate.
     * @return The certificate in PDF format
     */
    @GET
    @Path("/signed/{intygId}/pdf")
    @Produces("application/pdf")
    public final Response getSignedIntygAsPdf(@PathParam(value = "intygId") final String intygId) {
        
        try {
            LOG.debug("Fetching signed intyg '{}' as PDF", intygId);

            IntygContentHolder intygAsExternal = intygService.fetchExternalIntygData(intygId);
                    
            String intygType = intygAsExternal.getMetaData().getType();
            
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);
            
            PdfResponse pdfResponse = moduleApi.pdf(new ExternalModelHolder(intygAsExternal.getContents()));
            
            String patientId = intygAsExternal.getMetaData().getPatientId();
            logService.logPrintOfIntyg(intygId, patientId);

            return Response.ok(pdfResponse.getPdfData()).header(CONTENT_DISPOSITION, buildPdfHeader(pdfResponse.getFilename())).build();
            
        } catch (ModuleException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }
    
    private String buildPdfHeader(String pdfFileName) {
        return "attachment; filename=\"" + pdfFileName + "\"";
    }
    
}
