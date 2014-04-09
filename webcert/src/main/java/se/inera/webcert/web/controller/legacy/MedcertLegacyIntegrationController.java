package se.inera.webcert.web.controller.legacy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.webcert.web.controller.AbstractIntegrationController;

/**
 * API controller for handling Medcert legacy integrations.
 * 
 * @author nikpet
 *
 */
@Path("/user")
public class MedcertLegacyIntegrationController extends AbstractIntegrationController {

    private static final String CERT_FK7263 = "fk7263";

    private static final Logger LOG = LoggerFactory.getLogger(MedcertLegacyIntegrationController.class);
        
    /**
     * Emulates the Medcert funtionality to create a new question for a specific
     * certificate. The user is redirected to the new location of the FK7263 view.
     * 
     * /medcert/web/user/question/create?careUnitId=IFV1239877878103N&
     * certificateId
     * =123456789&certificateSignedAt=1288180860000&patientName=Tolvan
     * +Tolvansson&patientSsn=19121212-1212
     * 
     * @return
     */
    @GET
    @Path("/question/create")
    public Response createNewQuestion(@Context UriInfo uriInfo, @QueryParam(value = "certificateId") String certificateId) {
        
        if (StringUtils.isBlank(certificateId)) {
            return buildMissingCertificateIdParameterErrorResponse(uriInfo);
        }
        
        LOG.debug("User is trying to create question for certificate '{}' thru legacy url", certificateId);

        return buildRedirectResponse(uriInfo, CERT_FK7263, certificateId);
    }

    /**
     * Emulates the Medcert funtionality to view questions for a specific
     * certificate. The user is now redirected to the new location of the FK7263 view.
     * 
     * http://localhost:8080/medcert/web/user/certificate/123456789/questions
     * 
     * @return
     */
    @GET
    @Path("/certificate/{certificateId}/questions")
    public Response viewCertificate(@Context UriInfo uriInfo, @PathParam(value = "certificateId") String certificateId) {

        if (StringUtils.isBlank(certificateId)) {
            return buildMissingCertificateIdParameterErrorResponse(uriInfo);
        }
        
        LOG.debug("User is trying to view certificate '{}' thru legacy url", certificateId);
        
        return buildRedirectResponse(uriInfo, CERT_FK7263, certificateId);
    }
        
    private Response buildMissingCertificateIdParameterErrorResponse(UriInfo uriInfo) {
        LOG.error("URI '{}' was called with the certificateId parameter missing", uriInfo.getBaseUri().getRawPath());
        return Response.status(Status.BAD_REQUEST).entity("Missing parameter certificateId").build();
    }
}
