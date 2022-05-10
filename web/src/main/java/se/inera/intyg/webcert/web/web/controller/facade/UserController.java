/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.facade.GetUserResourceLinks;
import se.inera.intyg.webcert.web.service.facade.user.UserService;
import se.inera.intyg.webcert.web.service.facade.user.UserTabsService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.UserResponseDTO;

@Path("/user")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final UserService userService;

    private final GetUserResourceLinks getUserResourceLinks;

    private final WebCertUserService webCertUserService;

    private final UserTabsService userTabsService;

    @Autowired
    public UserController(UserService userService, GetUserResourceLinks getUserResourceLinks,
                          WebCertUserService webCertUserService, UserTabsService userTabsService) {
        this.userService = userService;
        this.getUserResourceLinks = getUserResourceLinks;
        this.webCertUserService = webCertUserService;
        this.userTabsService = userTabsService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getUser() {
        LOG.debug("Getting logged in user");
        final var loggedInUser = userService.getLoggedInUser();
        final var resourceLinks = getUserResourceLinks.get(webCertUserService.getUser());
        return Response.ok(UserResponseDTO.create(loggedInUser, resourceLinks)).build();
    }

    @Path("/tabs")
    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getUserTabs() {
        LOG.debug("Getting user tabs");
        final var tabs = userTabsService.get();
        return Response.ok(tabs).build();
    }
}
