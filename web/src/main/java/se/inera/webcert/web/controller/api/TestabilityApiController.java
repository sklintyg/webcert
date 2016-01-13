package se.inera.webcert.web.controller.api;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.integration.fmb.services.FmbService;
import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.service.user.WebCertUserService;
import se.inera.webcert.service.user.dto.WebCertUser;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Rest interface only used for testing and in dev environments. It seems like it must be in
 * the same Spring context as the rest of the webservices to get access to the security context.
 */
public class TestabilityApiController {

    private static final Logger LOG = LoggerFactory.getLogger(TestabilityApiController.class);

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private FmbService fmbService;

    @GET
    @Path("/userrole")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonPropertyDescription("Get the roles for user in session")
    public Response getUserRoles() {
        final WebCertUser user = webCertUserService.getUser();
        final Map<String, UserRole> roles = user.getRoles();
        final Set<String> roleStrings = roles.keySet();
        return Response.ok(roleStrings).build();
    }

    /**
     * Set the role for current user. Using a GET to change a state might not be
     * recommended. However, it is a very convenient way to change the user role
     * from the browser and it is also the only way I could figure out to invoke
     * it from the browser session in the Fitnesse tests.
     */
    @GET
    @Path("/userrole/{role}")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonPropertyDescription("Set the roles for user in session")
    public Response setUserRole(@PathParam("role") UserRole newRole) {
        webCertUserService.updateUserRoles(new String[]{newRole.name()});
        return Response.ok().build();
    }

    /**
     * Populate FMB data using the configured endpoint. Using a GET to update data might
     * not be recommended. However, it is a very convenient way to populate FMB data from
     * the browser without waiting for the automatic population that happens once each
     * day. It is also the only way I could figure out to invoke it from the browser
     * session in the Fitnesse tests.
     */
    @GET
    @Path("/updatefmbdata")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonPropertyDescription("Update FMB data")
    public Response setUserRole() {
        fmbService.updateData();
        return Response.ok().build();
    }

}
