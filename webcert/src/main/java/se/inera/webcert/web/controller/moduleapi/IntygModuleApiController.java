package se.inera.webcert.web.controller.moduleapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException;
import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.integration.rest.dto.CertificateContentHolder;
import se.inera.webcert.service.IntygService;
import se.inera.webcert.service.LogService;

/**
 * @author andreaskaltenbach
 */
public class IntygModuleApiController {

    private static final Logger LOG = LoggerFactory.getLogger(IntygModuleApiController.class);

    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    @Autowired
    private IntygService intygService;

    @Autowired
    private ModuleRestApiFactory moduleApiFactory;
    
    @Autowired
    private LogService logService;

    @GET
    @Path("/{intygId}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response intyg(@PathParam("intygId") String intygId) {
        
        CertificateContentHolder certificateContentHolder = intygService.fetchIntygData(intygId);
        String patientId = certificateContentHolder.getCertificateContentMeta().getPatientId();
        
        logService.logReadOfIntyg(intygId, patientId);
        
        return Response.ok().entity(certificateContentHolder).build();
    }

    /**
     * Return the certificate identified by the given id as PDF.
     * 
     * @param intygId
     *            - the globally unique id of a certificate.
     * @return The certificate in PDF format
     */
    @GET
    @Path("/{intygId}/pdf")
    @Produces("application/pdf")
    public final Response getCertificatePdf(@PathParam(value = "intygId") final String intygId) {
        LOG.debug("getCertificatePdf: {}", intygId);

        CertificateContentHolder certificateContentHolder;

        String patientId = null;
        
        try {
            certificateContentHolder = intygService.fetchExternalIntygData(intygId);
            patientId = certificateContentHolder.getCertificateContentMeta().getPatientId();
        } catch (ExternalWebServiceCallFailedException ex) {
            return Response.status(INTERNAL_SERVER_ERROR).build();
        }

        Response pdf = fetchPdf(certificateContentHolder);

        if (isNotOk(pdf)) {
            LOG.error("Failed to get PDF for certificate " + intygId + " from inera-certificate.");
            return Response.status(pdf.getStatus()).build();
        }

        logService.logPrintOfIntyg(intygId, patientId);
        
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
