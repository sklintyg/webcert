package se.inera.webcert.web.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.service.privatlakaravtal.AvtalService;
import se.inera.webcert.web.controller.AbstractApiController;
import se.inera.webcert.web.controller.api.dto.ChangeSelectedUnitRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Controller for accessing the users security context.
 *
 * @author npet
 *
 */
@Path("/anvandare")
public class UserApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(UserApiController.class);

    @Autowired
    AvtalService avtalService;

    /**
     * Retrieves the security context of the logged in user as JSON.
     *
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getUser() {
        WebCertUser user = getWebCertUserService().getWebCertUser();
        return Response.ok(user.getAsJson()).build();
    }

    /**
     * Changes the selected care unit in the security context for the logged in user.
     *
     * @param request
     * @return
     */
    @POST
    @Path("/andraenhet")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response changeSelectedUnitOnUser(ChangeSelectedUnitRequest request) {

        WebCertUser user = getWebCertUserService().getWebCertUser();

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

    /**
     * Retrieves the security context of the logged in user as JSON.
     *
     * @return
     */
    @PUT
    @Path("/godkannavtal")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response godkannAvtal() {
        WebCertUser user = getWebCertUserService().getWebCertUser();
        if (user != null) {
            avtalService.approveLatestAvtal(user.getHsaId());
        }
        return Response.ok().build();
    }

    /**
     * Deletes privatlakaravtal approval for the specified user.
     *
     * @return
     */
    @DELETE
    @Path("/privatlakaravtal")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response taBortAvtalsGodkannande() {
        WebCertUser user = getWebCertUserService().getWebCertUser();
        if (user != null) {
            avtalService.removeApproval(user.getHsaId());
            return Response.ok().build();
        }
        return Response.notModified().build();
    }
}
