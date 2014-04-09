package se.inera.webcert.web.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

public abstract class AbstractIntegrationController {

    private static final String PARAM_CERT_ID = "certId";

    private static final String PARAM_CERT_TYPE = "certType";
        
    protected String urlBaseTemplate;
    
    protected String urlFragmentTemplate;
    
    protected Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String certificateId) {
        
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
        
        Map<String, Object> urlParams = new HashMap<String, Object>();
        urlParams.put(PARAM_CERT_TYPE, certificateType);
        urlParams.put(PARAM_CERT_ID, certificateId);
        
        URI location = uriBuilder.replacePath(urlBaseTemplate).fragment(urlFragmentTemplate).buildFromMap(urlParams);

        return Response.status(Status.MOVED_PERMANENTLY).location(location).build();
    }
        
    public void setUrlBaseTemplate(String urlBaseTemplate) {
        this.urlBaseTemplate = urlBaseTemplate;
    }

    public void setUrlFragmentTemplate(String urlFragmentTemplate) {
        this.urlFragmentTemplate = urlFragmentTemplate;
    }
}
