package se.inera.intyg.webcert.web.web.controller.testability;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.privatlakaravtal.repository.AvtalRepository;
import se.inera.intyg.webcert.persistence.privatlakaravtal.repository.GodkantAvtalRepository;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Set;

@Transactional
@Api(value = "services anvandare", description = "REST API för testbarhet - Användare")
@Path("/anvandare")
public class UserResource {

    @Autowired
    private AvtalRepository avtalRepository;

    @Autowired
    private GodkantAvtalRepository godkantAvtalRepository;

    @Autowired
    private WebCertUserService webCertUserService;


    @PUT
    @Path("/godkannavtal/{hsaId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response godkannAvtal(@PathParam("hsaId") String hsaId) {
        int avtalVersion = avtalRepository.getLatestAvtalVersion();
        godkantAvtalRepository.approveAvtal(hsaId, avtalVersion);
        return Response.ok().build();
    }

    @PUT
    @Path("/avgodkannavtal/{hsaId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response avgodkannAvtal(@PathParam("hsaId") String hsaId) {
        godkantAvtalRepository.removeAllUserApprovments(hsaId);
        return Response.ok().build();
    }

    @GET
    @Path("/origin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrigin() {
        final WebCertUser user = webCertUserService.getUser();
        final String currentOrigin = user.getOrigin();
        return Response.ok(currentOrigin).build();
    }

    @GET
    @Path("/origin/{origin}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setOrigin(@PathParam("origin") String origin) {
        webCertUserService.updateOrigin(origin);
        return Response.ok().build();
    }

}
