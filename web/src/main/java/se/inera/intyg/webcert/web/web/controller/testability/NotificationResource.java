/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.webcert.web.web.controller.testability.IntygResource.UTF_8_CHARSET;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.testability.dto.NotificationRequestDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.util.CreateCertificateTestabilityUtil;

@Path("/notification")
@Transactional
public class NotificationResource {

    @Autowired
    private CreateCertificateTestabilityUtil certificateTestabilityUtil;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UtkastService utkastService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private HandelseRepository handelseRepository;

    @Autowired
    private NotificationRedeliveryRepository redeliveryRepository;

    @POST
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/updateandnotify")
    public Response updateCertificate(@RequestBody @NotNull List<NotificationRequestDTO> notificationRequest) {
        updateDraft(notificationRequest);
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/deliverystatus/{certificateId}")
    public Response checkIfEventPersisted(@PathParam("certificateId") String certificateId) {
        final var deliveryStatus = getDeliveryStatus(certificateId);
        return Response.ok(deliveryStatus).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/clearevents")
    public Response clearEvents(@RequestBody @NotNull List<String> certificateIds) {
        clearEventsForCertificateIds(certificateIds);
        return Response.ok().build();
    }

    private void updateDraft(List<NotificationRequestDTO> notificationRequests) {
        for (var notificationRequest : notificationRequests) {
            final var certificateRequest = notificationRequest.getCertificateRequestDTO();
            final var updatedDraft = updateDraft(notificationRequest.getCertificate(), notificationRequest.getCertificateRequestDTO());
            sendNotification(certificateRequest, updatedDraft);
        }
    }

    private Utkast updateDraft(Certificate certificate, CreateCertificateRequestDTO certificateRequest) {
        final var utkast = utkastService.getDraft(certificate.getMetadata().getId(), false);
        final var hoSPersonal = certificateTestabilityUtil.getHoSPerson(certificateRequest.getPersonId(),
            certificateRequest.getUnitId());

        certificateTestabilityUtil.updateCertificate(certificateRequest, certificate);
        utkast.setModel(certificateTestabilityUtil.getJsonFromCertificate(certificate, utkast.getModel()));
        certificateTestabilityUtil.updateCertificateWithRequestedStatus(certificateRequest, hoSPersonal, utkast);

        return utkastRepository.save(utkast);
    }

    private void sendNotification(CreateCertificateRequestDTO certificateRequest, Utkast updatedDraft) {
        if (certificateRequest.getStatus() == CertificateStatus.SIGNED) {
            notificationService.sendNotificationForDraftSigned(updatedDraft);
            return;
        }
        notificationService.sendNotificationForDraftChanged(updatedDraft);
    }

    public NotificationDeliveryStatusEnum getDeliveryStatus(String certificatId) {
        final var events =  handelseRepository.findByIntygsId(certificatId);
        if (events.isEmpty()) {
            return null;
        }
        return events.get(0).getDeliveryStatus();
    }

    private void clearEventsForCertificateIds(List<String> certificateIds) {
        for (var certificateId : certificateIds) {
            final var events = handelseRepository.findByIntygsId(certificateId);
            final var eventsForRedelivery = events.stream()
                .filter(event -> event.getDeliveryStatus() == NotificationDeliveryStatusEnum.RESEND).collect(Collectors.toList());
            deleteRedeliveries(eventsForRedelivery);
            handelseRepository.deleteAll(events);
            }
        }

    private void deleteRedeliveries(List<Handelse> events) {
        for (var event : events) {
            redeliveryRepository.findByEventId(event.getId()).ifPresent(redelivery -> redeliveryRepository.delete(redelivery));
        }
    }
}
