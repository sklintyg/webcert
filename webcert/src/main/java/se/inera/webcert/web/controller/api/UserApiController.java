package se.inera.webcert.web.controller.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.web.controller.api.dto.ChangeSelectedUnitRequest;
import se.inera.webcert.web.service.WebCertUserService;

@Path("/user")
public class UserApiController {

    protected static final String UTF_8_CHARSET = ";charset=utf-8";
    
    /**
     * Helper service to get current user.
     */    
    @Autowired
    protected WebCertUserService webCertUserService;
    
    private static Logger LOG = LoggerFactory.getLogger(UserApiController.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getUser() {
        WebCertUser user = webCertUserService.getWebCertUser();
        return Response.ok(user.getAsJson()).build();
    }
    
    @POST
    @Path("/changeunit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response changeSelectedUnitOnUser(ChangeSelectedUnitRequest request) {

        WebCertUser user = webCertUserService.getWebCertUser();

        LOG.debug("Attempting to change selected unit for user '{}', currently selected unit is '{}'", user.getHsaId(),
                user.getValdVardenhet().getId());
        
        boolean changeSuccess = user.changeValdVardenhet(request.getId());

        if (!changeSuccess) {
            LOG.error("Unit '{}' is not present in the MIUs for user '{}'", request.getId(), user.getHsaId());
            return Response.status(Status.BAD_REQUEST).entity("Unit change failed").build();
        }

        LOG.debug("Seleced vardenhet is now '{}'", user.getValdVardenhet().getId());

        return Response.ok(user.getAsJson()).build();
    }
}
