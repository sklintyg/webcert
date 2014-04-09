package se.inera.webcert.web.controller.legacy;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * API controller for handling Medcert legacy integrations.
 * 
 * @author nikpet
 *
 */
@Component
public class MedcertLegacyApiController {

    private static final String CERT_FK7263 = "fk7263";

    private static final String PARAM_CERT_ID = "certId";

    private static final String PARAM_CERT_TYPE = "certType";

    private static final String FK7263_PATH_TEMPLATE = "/web/dashboard";
    private static final String FK7263_FRAGMENT_TEMPLATE = "/fk7263/view/{certId}";

    private static final Logger LOG = LoggerFactory.getLogger(MedcertLegacyApiController.class);
    
    @Value("${certificate.view.url.base}")
    private String urlBaseTemplate;
    
    @Value("${certificate.view.url.fragment.template}")
    private String urlFragmentTemplate;

    /**
     * Emulates the Medcert functionality to view questions for a specific
     * certificate. The user is now redirected to the new location of the FK7263 view.
     * 
     * http://localhost:8080/mvisa/intyg/123456789/fragor
     * 
     * @return
     */
    @GET
    @Path("/intyg/{certificateId}/fragor")
    public Response viewCertificate(@Context UriInfo uriInfo, @PathParam(value = "certificateId") String certificateId) {

        if (StringUtils.isBlank(certificateId)) {
            return buildMissingCertificateIdParameterErrorResponse(uriInfo);
        }
        
        LOG.debug("User is trying to view certificate '{}' thru legacy url", certificateId);
        
        return buildRedirectResponse(uriInfo, certificateId);
    }
    
    private Response buildRedirectResponse(UriInfo uriInfo, String certificateId) {
        
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
        
        Map<String, Object> urlParams = new HashMap<String, Object>();
        urlParams.put(PARAM_CERT_TYPE, CERT_FK7263);
        urlParams.put(PARAM_CERT_ID, certificateId);
        
        URI location = uriBuilder.replacePath(urlBaseTemplate).fragment(urlFragmentTemplate).buildFromMap(urlParams);

        return Response.status(Status.TEMPORARY_REDIRECT).location(location).build();
    }
    
    private Response buildMissingCertificateIdParameterErrorResponse(UriInfo uriInfo) {
        LOG.error("URI '{}' was called with the certificateId parameter missing", uriInfo.getBaseUri().getRawPath());
        return Response.status(Status.BAD_REQUEST).entity("Missing parameter certificateId").build();
    }
}
