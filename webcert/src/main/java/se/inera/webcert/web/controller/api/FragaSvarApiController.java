package se.inera.webcert.web.controller.api;


import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.security.WebCertUser;
import se.inera.webcert.service.FragaSvarService;
import se.inera.webcert.web.service.WebCertUserService;



public class FragaSvarApiController {

    private static final Logger LOG = LoggerFactory.getLogger(FragaSvarApiController.class);

    /**
     * Helper service to get current user.
     */
    @Autowired
    private FragaSvarService fragaSvarService;
    
    @Autowired
    private WebCertUserService webCertUserService;
    
    
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<FragaSvar> list() {
        WebCertUser user = webCertUserService.getWebCertUser();
        return fragaSvarService.getFragaSvar(user.getVardEnheter());
    }
    
    @PUT
    @Path("/{enhets-id}/{fragasvar-id}/answer" )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response answer(@PathParam( "enhets-id" ) final String enhetsId, @PathParam( "fragasvar-id" ) final Long frageSvarId, FragaSvar fragaSvar) {
        FragaSvar fragaSvarResponse =  fragaSvarService.saveSvar(fragaSvar);
        return Response.ok(fragaSvarResponse).build();
    }
}
