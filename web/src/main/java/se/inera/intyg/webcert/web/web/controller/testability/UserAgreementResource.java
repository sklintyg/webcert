package se.inera.intyg.webcert.web.web.controller.testability;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.privatlakaravtal.repository.AvtalRepository;
import se.inera.intyg.webcert.persistence.privatlakaravtal.repository.GodkantAvtalRepository;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Transactional
@Api(value = "services anvandare", description = "REST API för testbarhet - Användare")
@Path("/anvandare")
public class UserAgreementResource {

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

}
