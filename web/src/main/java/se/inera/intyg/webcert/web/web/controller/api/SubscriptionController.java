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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.subscription.SubscriptionService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;

@Path("/subscription")
@Api(value = "subscription", produces = MediaType.APPLICATION_JSON)
public class SubscriptionController extends AbstractApiController {

    @Autowired
    private SubscriptionService subscriptionService;

    @GET
    @Path("/acknowledgeSubscriptionModal")
    @Produces(MediaType.APPLICATION_JSON)
    @PerformanceLogging(eventAction = "subscription-acknowledge-subscription-modal", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public ResponseEntity<String> acknowledgeSubscriptionModal() {
        final var webCertUser = getWebCertUserService().getUser();
        subscriptionService.acknowledgeSubscriptionModal(webCertUser);
        return ResponseEntity.ok().build();
    }
}
