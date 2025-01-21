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
package se.inera.intyg.webcert.web.web.controller.testability;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventRepository;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;

@Path("event")
public class EventResource {

    @Autowired
    HandelseRepository handelseRepository;

    @Autowired
    NotificationRedeliveryRepository redeliveryRepository;

    @Autowired
    CertificateEventRepository certificateEventRepository;


    @GET
    @Path("/eventCount")
    @Produces(MediaType.APPLICATION_JSON)
    public Long getEventCountForCertificateIds(List<String> certificateIds) {
        final var events = handelseRepository.findAll();
        return events.stream().filter(event -> certificateIds.contains(event.getIntygsId())).count();
    }

    @DELETE
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteEventsByCertificateIds(List<String> certificateIds) {
        final var events = handelseRepository.findAll();
        final var eventsForDeletion = events.stream()
            .filter(event -> certificateIds.contains(event.getIntygsId()))
            .collect(Collectors.toList());
        handelseRepository.deleteAll(eventsForDeletion);

        return Response.ok().build();
    }

    @GET
    @Path("/redeliveryCount")
    @Produces(MediaType.APPLICATION_JSON)
    public Long getRedeliveryCountForCertificateIds(List<String> certificateIds) {
        final List<Long> eventIds = getEventIds(certificateIds);
        final var redeliveries = redeliveryRepository.findAll();
        return redeliveries.stream().filter(redelivery -> eventIds.contains(redelivery.getEventId())).count();
    }

    @DELETE
    @Path("/redelivery")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRedeliveriesByCertificateIds(List<String> certificateIds) {
        final List<Long> eventIds = getEventIds(certificateIds);
        final var redeliveries = redeliveryRepository.findAll();
        final var redeliveriesForDeletion = redeliveries.stream().filter(redelivery -> eventIds.contains(redelivery.getEventId()))
            .collect(Collectors.toList());
        redeliveryRepository.deleteAll(redeliveriesForDeletion);

        return Response.ok().build();
    }

    @GET
    @Path("/certificateEventCount")
    @Produces(MediaType.APPLICATION_JSON)
    public Long getCertificateEventCountForCertificateIds(List<String> certificateIds) {
        final var certificateEvents = certificateEventRepository.findAll();
        return certificateEvents.stream().filter(event -> certificateIds.contains(event.getCertificateId())).count();
    }

    @DELETE
    @Path("/certificateEvent")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCertificateEventsByCertificateIds(List<String> certificateIds) {
        final var certificateEvents = certificateEventRepository.findAll();
        final var certificateEventsForDeletion = certificateEvents.stream()
            .filter(event -> certificateIds.contains(event.getCertificateId()))
            .collect(Collectors.toList());
        certificateEventRepository.deleteAll(certificateEventsForDeletion);

        return Response.ok().build();
    }

    private List<Long> getEventIds(List<String> certificateIds) {
        final var events = handelseRepository.findAll();
        return events.stream().filter(event -> certificateIds.contains(event.getIntygsId())).map(Handelse::getId)
            .collect(Collectors.toList());
    }
}
