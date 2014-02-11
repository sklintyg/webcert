package se.inera.webcert.web.controller.moduleapi;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException;
import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.integration.rest.dto.CertificateContentHolder;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.IntygService;
import se.inera.webcert.service.draft.IntygDraftService;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.dto.DraftValidationMessage;
import se.inera.webcert.web.controller.moduleapi.dto.DraftValidationStatus;
import se.inera.webcert.web.controller.moduleapi.dto.IntygDraftHolder;
import se.inera.webcert.web.controller.moduleapi.dto.SaveDraftResponse;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * @author andreaskaltenbach
 */
public class IntygModuleApiController {

    private static final String UTF_8 = "UTF-8";

    private static final Logger LOG = LoggerFactory.getLogger(IntygModuleApiController.class);

    private static final String UTF_8_CHARSET = ";charset=utf-8";
    
    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    @Autowired
    private IntygService intygService;

    @Autowired
    private IntygDraftService draftService;
    
    @Autowired
    private IntygRepository intygRepository;
    
    @Autowired
    private WebCertUserService userService;
    
    @Autowired
    private ModuleRestApiFactory moduleApiFactory;
      
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
    @Transactional
    public Response saveDraft(@PathParam("intygId") String intygId, byte[] bytes) {
                        
        LOG.debug("Saving Intyg with id {}", intygId);
        
        Intyg intyg = intygRepository.findOne(intygId);
        
        if (intyg == null) {
            LOG.warn("Intyg with id {} was not found", intygId);
            return Response.status(Status.NOT_FOUND).build();
        }
        
        String draftAsJson = fromBytesToString(bytes);
        
        String intygType = intyg.getIntygsTyp();
        
        DraftValidation draftValidation = draftService.validateDraft(intygId, intygType, draftAsJson);
        
        IntygsStatus intygStatus = (draftValidation.isDraftValid()) ? IntygsStatus.DRAFT_COMPLETE : IntygsStatus.DRAFT_INCOMPLETE;
                
        intyg.setModel(draftAsJson);
        intyg.setStatus(intygStatus);
        
        VardpersonReferens vardPersonRef = createVardpersonReferens();
        intyg.setSenastSparadAv(vardPersonRef);
        
        intygRepository.save(intyg);
        
        SaveDraftResponse responseEntity = buildSaveDraftResponse(draftValidation);
        
        return Response.ok().entity(responseEntity).build();
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
            throw new RuntimeException("Could not convert the payload from bytes to String!");
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
        
        LOG.debug("Discarding Intyg with id {}", intygId);
        
        // TODO: Implement
        
        return Response.ok().build();
    }
    
    private VardpersonReferens createVardpersonReferens() {
        
        WebCertUser user = userService.getWebCertUser();
        
        VardpersonReferens vardPersonRef = new VardpersonReferens();
        vardPersonRef.setNamn(user.getNamn());
        vardPersonRef.setHsaId(user.getHsaId());
        
        return vardPersonRef;
    }
    
    /**
     * Candidate for removal since we do not really know if this is used anywhere.
     * 
     * @param intygId
     * @return
     */
    @Deprecated
    @GET
    @Path("/{intygId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getSignedIntyg(@PathParam("intygId") String intygId) {
        
        CertificateContentHolder fetchIntygData = intygService.fetchIntygData(intygId);
        
        return Response.ok().build();
    }

    /**
     * Return the certificate identified by the given id as PDF.
     * 
     * @param id
     *            - the globally unique id of a certificate.
     * @return The certificate in PDF format
     */
    @GET
    @Path("/{intygId}/pdf")
    @Produces("application/pdf")
    public final Response getCertificatePdf(@PathParam(value = "intygId") final String id) {
        LOG.debug("getCertificatePdf: {}", id);

        CertificateContentHolder certificateContentHolder;

        try {
            certificateContentHolder = intygService.fetchExternalIntygData(id);
        } catch (ExternalWebServiceCallFailedException ex) {
            return Response.status(INTERNAL_SERVER_ERROR).build();
        }

        Response pdf = fetchPdf(certificateContentHolder);

        if (isNotOk(pdf)) {
            LOG.error("Failed to get PDF for certificate " + id + " from inera-certificate.");
            return Response.status(pdf.getStatus()).build();
        }

        String filenameHeader = pdf.getHeaderString(CONTENT_DISPOSITION); // filename=...

        return Response.ok(pdf.getEntity()).header(CONTENT_DISPOSITION, "attachment; " + filenameHeader).build();
    }

    private boolean isNotOk(Response response) {
        return response.getStatus() != OK.getStatusCode();
    }

    private Response fetchPdf(CertificateContentHolder certificateContentHolder) {
        ModuleRestApi api = moduleApiFactory.getModuleRestService(certificateContentHolder.getCertificateContentMeta()
                .getType());
        return api.pdf(certificateContentHolder);
    }
}
