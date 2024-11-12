/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.authtestability;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.swagger.annotations.Api;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

/**
 * Rest interface only used for testing and in dev environments. It seems like it must be in
 * the same Spring context as the rest of the webservices to get access to the security context.
 */
@Api(value = "user service", produces = MediaType.APPLICATION_JSON)
@Path("/")
public class UserResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    @Autowired
    private WebCertUserService webCertUserService;

    @GET
    @Path("/role")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonPropertyDescription("Get the roles for user in session")
    @PrometheusTimeMethod
    public Response getUserRoles() {
        final WebCertUser user = webCertUserService.getUser();
        final Map<String, Role> roles = user.getRoles();
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
    @Path("/role/{role}")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonPropertyDescription("Set the roles for user in session")
    @PrometheusTimeMethod
    public Response setUserRole(@PathParam("role") String role) {
        webCertUserService.updateUserRole(role);
        return Response.ok().build();
    }

    @GET
    @Path("/origin")
    @Produces(MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    public Response getOrigin() {
        final WebCertUser user = webCertUserService.getUser();
        final String currentOrigin = user.getOrigin();
        return Response.ok(currentOrigin).build();
    }

    /**
     * Set current user's request origin. Using a GET to change a state might not be
     * recommended. However, it is a very convenient way to change the request origin
     * from the browser and it is also the only way I could figure out to invoke
     * it from the browser session in the Fitnesse tests.
     */
    @GET
    @Path("/origin/{origin}")
    @Produces(MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    public Response setOrigin(@PathParam("origin") String origin) {
        webCertUserService.updateOrigin(origin);
        return Response.ok().build();
    }

    @GET
    @Path("/preferences/delete")
    @PrometheusTimeMethod
    public Response deleteUserPreferences() {
        webCertUserService.deleteUserPreferences();
        return Response.ok().build();
    }

    @GET
    @Path("/preferences")
    @Produces(MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    public Response getUserPreferences() {
        final var prefs = webCertUserService.getUser().getAnvandarPreference();
        return Response.ok(prefs).build();
    }

    @GET
    @Path("/parameters")
    @Produces(MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    public Response getParameters() {
        return Response.ok(webCertUserService.getUser().getParameters()).build();
    }

    @POST
    @Path("/parameters/sjf")
    @PrometheusTimeMethod
    public Response setSjf() {
        webCertUserService.getUser()
            .setParameters(
                new IntegrationParameters(null, null, null, null, null, null, null, null, null, true, false, false, true, null));
        return Response.ok().build();
    }

    /**
     * Use this endpoint to specify a "reference" DJUPINTEGRATION parameter in tests.
     *
     * @param refValue Whatever string you want to specifiy as reference.
     * @return 200 OK unless there's a problem.
     */
    @GET
    @Path("/parameters/ref/{refValue}")
    @PrometheusTimeMethod
    public Response setRef(@PathParam("refValue") String refValue) {
        webCertUserService.getUser()
            .setParameters(
                new IntegrationParameters(refValue, null, null, null, null, null, null, null, null, true, false, false, true, null));
        return Response.ok().build();
    }

    @PUT
    @Path("/parameters/launchId/{launchId}")
    @PrometheusTimeMethod
    public Response setLaunchId(@PathParam("launchId") String launchId) {
        webCertUserService.getUser()
            .setParameters(
                new IntegrationParameters(null, null, null, null, null, null, null, null, null, true, false, false, true,
                    launchId));
        return Response.ok().build();
    }

    @GET
    @Path("/features")
    @Produces(MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    public Response getFeaturesForUser() {
        Map<String, Feature> features = webCertUserService.getUser().getFeatures();
        return Response.ok(features).build();
    }

    @PUT
    @Path("/personid")
    @Produces(MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    public Response getFeaturesForUser(String personId) {
        String oldPersonId = webCertUserService.getUser().getPersonId();
        webCertUserService.getUser().setPersonId(personId);
        LOG.info("Changed user 'personId' to '{}', was '{}'.", personId, oldPersonId);
        return Response.ok().build();
    }

    @GET
    @Path("/subscriptionInfo")
    @Produces(MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    public Response getSubscriptionInfo() {
        final var info = webCertUserService.getUser().getSubscriptionInfo();
        return Response.ok(info).build();
    }
}
