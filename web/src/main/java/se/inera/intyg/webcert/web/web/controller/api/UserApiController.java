package se.inera.intyg.webcert.web.web.controller.api;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogServiceImpl;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.ChangeSelectedUnitRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.WebUserFeaturesRequest;

/**
 * Controller for accessing the users security context.
 *
 * @author npet
 *
 */
@Path("/anvandare")
@Api(value = "anvandare", description = "REST API för användarhantering", produces = MediaType.APPLICATION_JSON)
public class UserApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(UserApiController.class);

    @Autowired
    private AvtalService avtalService;

    @Autowired
    private WebcertFeatureService featureService;

    @Autowired
    private MonitoringLogServiceImpl monitoringService;

    /**
     * Retrieves the security context of the logged in user as JSON.
     *
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getUser() {
        WebCertUser user = getWebCertUserService().getUser();
        return Response.ok(user.getAsJson()).build();
    }

    @PUT
    @Path("/features")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response userFeatures(WebUserFeaturesRequest webUserFeaturesRequest) {
        WebCertUser user = getWebCertUserService().getUser();
        Set<String> mutFeatures = new HashSet<>(user.getAktivaFunktioner());
        updateFeatures(webUserFeaturesRequest.isJsLoggning(), WebcertFeature.JS_LOGGNING.getName(), mutFeatures);
        updateFeatures(webUserFeaturesRequest.isJsMinified(), WebcertFeature.JS_MINIFIED.getName(), mutFeatures);
        user.setAktivaFunktioner(mutFeatures);
        return Response.ok(mutFeatures).build();
    }

    private void updateFeatures(boolean feature, String name, Set<String>features) {
        if (feature) {
            features.add(name);
        } else {
            features.remove(name);
        }
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

        WebCertUser user = getWebCertUserService().getUser();

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
        WebCertUser user = getWebCertUserService().getUser();
        if (user != null) {
            avtalService.approveLatestAvtal(user.getHsaId(), user.getPersonId());
            user.setPrivatLakareAvtalGodkand(true);
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
        WebCertUser user = getWebCertUserService().getUser();
        if (user != null) {
            avtalService.removeApproval(user.getHsaId());
            return Response.ok().build();
        }
        return Response.notModified().build();
    }

    @GET
    @Path("/latestavtal")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getAvtal() {
         Avtal avtal = avtalService.getLatestAvtal();
        return Response.ok(avtal).build();
    }
}
