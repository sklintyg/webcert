/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.web.controller.api;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.inera.intyg.webcert.common.model.WebcertFeature;
import se.inera.intyg.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.ChangeSelectedUnitRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.WebUserFeaturesRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.WebUserPreferenceStorageRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.HashSet;
import java.util.Set;

/**
 * Controller for accessing the users security context.
 *
 * @author npet
 */
@Path("/anvandare")
@Api(value = "anvandare", description = "REST API för användarhantering", produces = MediaType.APPLICATION_JSON)
public class UserApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(UserApiController.class);

    @Autowired
    private AvtalService avtalService;

    @Autowired
    private WebcertFeatureService webcertFeatureService;

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
        Set<String> mutFeatures = new HashSet<>(user.getFeatures());
        updateFeatures(webUserFeaturesRequest.isJsLoggning(), WebcertFeature.JS_LOGGNING.getName(), mutFeatures);
        updateFeatures(webUserFeaturesRequest.isJsMinified(), WebcertFeature.JS_MINIFIED.getName(), mutFeatures);
        user.setFeatures(mutFeatures);
        return Response.ok(mutFeatures).build();
    }

    private void updateFeatures(boolean feature, String name, Set<String> features) {
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
                user.getValdVardenhet() != null ? user.getValdVardenhet().getId() : "(none)");

        boolean changeSuccess = user.changeValdVardenhet(request.getId());

        if (!changeSuccess) {
            LOG.error("Unit '{}' is not present in the MIUs for user '{}'", request.getId(), user.getHsaId());
            return Response.status(Status.BAD_REQUEST).entity("Unit change failed").build();
        }

        user.setFeatures(webcertFeatureService.getActiveFeatures(user.getValdVardenhet().getId(), user.getValdVardgivare().getId()));

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

    @GET
    @Path("/ping")
    public Response clientPing() {
        // Any active user session will be extended just by accessing an endpoint.
        LOG.debug("wc-client pinged server");
        return Response.ok().build();
    }

    @PUT
    @Path("/preferences")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response storeUserMetdataEntry(WebUserPreferenceStorageRequest request) {
        LOG.debug("User stored user preference entry for key: " + request.getKey());
        getWebCertUserService().storeUserPreference(request.getKey(), request.getValue());
        return Response.ok().build();
    }

    @DELETE
    @Path("/preferences/{key}")
    public Response deleteUserPreferenceEntry(@PathParam("key") String prefKey) {
        LOG.debug("User deleted user preference entry for key: " + prefKey);
        getWebCertUserService().deleteUserPreference(prefKey);
        return Response.ok().build();
    }

    @GET
    @Path("/logout")
    public Response logoutUserAfterTimeout() {
        getWebCertUserService().scheduleSessionRemoval(getSessionId(), getHttpSession());
        return Response.ok().build();
    }

    @GET
    @Path("/logout/cancel")
    public Response cancelLogout() {
        getWebCertUserService().cancelScheduledLogout(getSessionId());
        return Response.ok().build();
    }

    private HttpSession getHttpSession() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            if (request != null) {
                return request.getSession();
            }
        }
        return null;
    }

    private String getSessionId() {
        return RequestContextHolder.currentRequestAttributes().getSessionId();
    }
}
