package se.inera.webcert.web.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractIntegrationController {

    private static final String PARAM_CERT_ID = "certId";

    private static final String PARAM_CERT_TYPE = "certType";
    
    @Value("${certificate.view.url.base}")
    private String urlBaseTemplate;
    
    @Value("${certificate.view.url.fragment.template}")
    private String urlFragmentTemplate;
    
    protected Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String certificateId) {
        
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
        
        Map<String, Object> urlParams = new HashMap<String, Object>();
        urlParams.put(PARAM_CERT_TYPE, certificateType);
        urlParams.put(PARAM_CERT_ID, certificateId);
        
        URI location = uriBuilder.replacePath(urlBaseTemplate).fragment(urlFragmentTemplate).buildFromMap(urlParams);

        return Response.status(Status.MOVED_PERMANENTLY).location(location).build();
    }    
}
