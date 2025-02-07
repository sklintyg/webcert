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

package se.inera.intyg.webcert.web.web.controller.internalapi;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.sendnotification.SendNotificationService;
import se.inera.intyg.webcert.web.service.sendnotification.SendNotificationsForCareGiverService;
import se.inera.intyg.webcert.web.service.sendnotification.SendNotificationsForCertificatesService;
import se.inera.intyg.webcert.web.service.sendnotification.SendNotificationsForUnitsService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCareGiverRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCertificatesRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForUnitsRequestDTO;

@Path("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final SendNotificationsForCertificatesService sendNotificationsForCertificatesService;
    private final SendNotificationService sendNotificationService;
    private final SendNotificationsForUnitsService sendNotificationsForUnitsService;
    private final SendNotificationsForCareGiverService sendNotificationsForCareGiverService;

    @POST
    @Path("/certificates")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public SendNotificationResponseDTO sendNotificationsForCertificates(@RequestBody SendNotificationsForCertificatesRequestDTO request) {
        return sendNotificationsForCertificatesService.send(request);
    }

    @POST
    @Path("/{notificationId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public SendNotificationResponseDTO sendNotification(@PathParam("notificationId") String notificationId) {
        return sendNotificationService.send(notificationId);
    }

    @POST
    @Path("/units")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public SendNotificationResponseDTO sendNotificationsForUnits(@RequestBody SendNotificationsForUnitsRequestDTO request) {
        return sendNotificationsForUnitsService.send(request);
    }

    @POST
    @Path("/caregiver/{careGiverId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public SendNotificationResponseDTO sendNotificationsForCareGiver(@PathParam("careGiverId") String careGiverId,
        @RequestBody SendNotificationsForCareGiverRequestDTO request) {
        return sendNotificationsForCareGiverService.send(careGiverId, request);
    }
}
