/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignatureService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.ChangeSelectedUnitRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.WebUserFeaturesRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.WebUserPreferenceStorageRequest;

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
    private CommonAuthoritiesResolver commonAuthoritiesResolver;

    @Autowired
    private DssSignatureService dssSignatureService;

    /**
     * Retrieves the security context of the logged in user as JSON.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "user-get-user", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getUser() {
        WebCertUser user = getWebCertUserService().getUser();

        var valdVardenhet = user.getValdVardenhet();
        if (valdVardenhet != null) {
            user.setUseSigningService(dssSignatureService.shouldUseSigningService(valdVardenhet.getId()));
        }
        return Response.ok(user.getAsJson()).build();
    }

    @PUT
    @Path("/features")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "user-set-user-features", eventType = MdcLogConstants.EVENT_TYPE_USER)
    public Response userFeatures(WebUserFeaturesRequest webUserFeaturesRequest) {
        WebCertUser user = getWebCertUserService().getUser();
        Map<String, Feature> mutFeatures = new HashMap<>(user.getFeatures());
        updateFeatures(webUserFeaturesRequest.isJsLoggning(), AuthoritiesConstants.FEATURE_JS_LOGGNING, mutFeatures);
        user.setFeatures(mutFeatures);
        return Response.ok(mutFeatures).build();
    }

    /**
     * Changes the selected care unit in the security context for the logged in user.
     */
    @POST
    @Path("/andraenhet")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "user-change-selected-unit", eventType = MdcLogConstants.EVENT_TYPE_USER)
    public Response changeSelectedUnitOnUser(ChangeSelectedUnitRequest request) {

        WebCertUser user = getWebCertUserService().getUser();

        LOG.debug("Attempting to change selected unit for user '{}', currently selected unit is '{}'", user.getHsaId(),
            user.getValdVardenhet() != null ? user.getValdVardenhet().getId() : "(none)");

        boolean changeSuccess = user.changeValdVardenhet(request.getId());

        if (!changeSuccess) {
            LOG.error("Unit '{}' is not present in the MIUs for user '{}'", request.getId(), user.getHsaId());
            return Response.status(Status.BAD_REQUEST).entity("Unit change failed").build();
        }

        var valdVardenhet = user.getValdVardenhet();
        if (valdVardenhet != null) {
            user.setUseSigningService(dssSignatureService.shouldUseSigningService(valdVardenhet.getId()));
        }

        user.setFeatures(commonAuthoritiesResolver.getFeatures(
            Arrays.asList(user.getValdVardenhet().getId(), user.getValdVardgivare().getId())));

        LOG.debug("Seleced vardenhet is now '{}'", user.getValdVardenhet().getId());

        return Response.ok(user.getAsJson()).build();
    }

    /**
     * Retrieves the security context of the logged in user as JSON.
     */
    @PUT
    @Path("/godkannavtal")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "user-approve-agreement", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response godkannAvtal() {
        WebCertUser user = getWebCertUserService().getUser();
        if (user != null) {
            avtalService.approveLatestAvtal(user.getHsaId(), user.getPersonId());
            user.setUserTermsApprovedOrSubscriptionInUse(true);
        }
        return Response.ok().build();
    }

    /**
     * Deletes privatlakaravtal approval for the specified user.
     */
    @DELETE
    @Path("/privatlakaravtal")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "user-remove-agreement-approval", eventType = MdcLogConstants.EVENT_TYPE_DELETION)
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
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "user-get-agreement", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getAvtal() {
        Optional<Avtal> avtal = avtalService.getLatestAvtal();
        return Response.ok(avtal.orElse(null)).build();
    }

    @GET
    @Path("/ping")
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "user-client-ping", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response clientPing() {
        // Any active user session will be extended just by accessing an endpoint.
        LOG.debug("wc-client pinged server");
        return Response.ok().build();
    }

    @PUT
    @Path("/preferences")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "user-store-metadata-entry", eventType = MdcLogConstants.EVENT_TYPE_USER)
    public Response storeUserMetdataEntry(WebUserPreferenceStorageRequest request) {
        LOG.debug("User stored user preference entry for key: " + request.getKey());
        getWebCertUserService().storeUserPreference(request.getKey(), request.getValue());
        return Response.ok().build();
    }

    @DELETE
    @Path("/preferences/{key}")
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "user-delete-user-preference-entry", eventType = MdcLogConstants.EVENT_TYPE_USER)
    public Response deleteUserPreferenceEntry(@PathParam("key") String prefKey) {
        LOG.debug("User deleted user preference entry for key: " + prefKey);
        getWebCertUserService().deleteUserPreference(prefKey);
        return Response.ok().build();
    }

    @GET
    @Path("/logout")
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "user-logout", eventType = MdcLogConstants.EVENT_TYPE_USER)
    public Response logoutUserAfterTimeout(@Context HttpServletRequest request) {
        HttpSession session = request.getSession();

        getWebCertUserService().scheduleSessionRemoval(session);

        return Response.ok().build();
    }

    @GET
    @Path("/logout/cancel")
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "user-cancel-logout", eventType = MdcLogConstants.EVENT_TYPE_USER)
    public Response cancelLogout(@Context HttpServletRequest request) {
        HttpSession session = request.getSession();

        getWebCertUserService().cancelScheduledLogout(session);

        return Response.ok().build();
    }

    private void updateFeatures(boolean active, String name, Map<String, Feature> features) {
        if (active) {
            Feature feature = new Feature();
            feature.setName(name);
            feature.setGlobal(true);
            features.put(name, feature);
        } else {
            features.remove(name);
        }
    }
}
