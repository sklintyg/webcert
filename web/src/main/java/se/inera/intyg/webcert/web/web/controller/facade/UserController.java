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
package se.inera.intyg.webcert.web.web.controller.facade;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.facade.ChangeUnitService;
import se.inera.intyg.webcert.web.service.facade.GetUserResourceLinks;
import se.inera.intyg.webcert.web.service.facade.impl.ChangeUnitException;
import se.inera.intyg.webcert.web.service.facade.user.UserService;
import se.inera.intyg.webcert.web.service.facade.user.UserStatisticsService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.UserResponseDTO;

@Path("/user")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final UserService userService;

    private final GetUserResourceLinks getUserResourceLinks;

    private final WebCertUserService webCertUserService;

    private final UserStatisticsService userStatisticsService;
    private final ChangeUnitService changeUnitService;

    @Autowired
    public UserController(UserService userService, GetUserResourceLinks getUserResourceLinks,
        UserStatisticsService userStatisticsService, WebCertUserService webCertUserService, ChangeUnitService changeUnitService) {
        this.userService = userService;
        this.getUserResourceLinks = getUserResourceLinks;
        this.webCertUserService = webCertUserService;
        this.userStatisticsService = userStatisticsService;
        this.changeUnitService = changeUnitService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "user-get-user", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getUser() {
        LOG.debug("Getting logged in user");
        final var loggedInUser = userService.getLoggedInUser();
        final var resourceLinks = getUserResourceLinks.get(webCertUserService.getUser());
        return Response.ok(UserResponseDTO.create(loggedInUser, resourceLinks)).build();
    }

    @Path("/statistics")
    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "user-get-user-tabs", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getUserTabs() {
        LOG.debug("Getting user statistics");
        final var result = userStatisticsService.getUserStatistics();
        return Response.ok(result).build();
    }

    @POST
    @Path("/unit/{unitHsaId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "user-change-unit", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response changeUnit(@PathParam("unitHsaId") @NotNull String unitHsaId) {
        LOG.debug("Changing care unit to {}", unitHsaId);
        try {
            final var updatedUser = changeUnitService.change(unitHsaId);
            final var resourceLinks = getUserResourceLinks.get(webCertUserService.getUser());
            return Response.ok(UserResponseDTO.create(updatedUser, resourceLinks)).build();
        } catch (ChangeUnitException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
