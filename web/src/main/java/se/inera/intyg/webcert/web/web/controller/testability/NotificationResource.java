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

import static se.inera.intyg.webcert.web.service.notification.NotificationEvent.NEW_ANSWER_FROM_CARE;
import static se.inera.intyg.webcert.web.service.notification.NotificationEvent.NEW_ANSWER_FROM_RECIPIENT;
import static se.inera.intyg.webcert.web.service.notification.NotificationEvent.QUESTION_FROM_CARE_HANDLED;
import static se.inera.intyg.webcert.web.web.controller.testability.IntygResource.UTF_8_CHARSET;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.notification.NotificationEvent;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.testability.dto.NotificationRequest;
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

    @Autowired
    private ArendeRepository arendeRepository;

    @POST
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/updateandnotify")
    public Response updateCertificate(@RequestBody @NotNull NotificationRequest notificationRequest) {
        final var certificateId = updateDraft(notificationRequest.getEventCode(), notificationRequest.getCertificateId());
        return Response.ok(certificateId).build();
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/cleararenden")
    public Response clearArenden(@RequestBody @NotNull List<String> certificateIds) {
        clearArendenForCertificateIds(certificateIds);
        return Response.ok().build();
    }

    private String updateDraft(HandelsekodEnum eventEnum, String certificateId) {
        switch (eventEnum) {
            case SIGNAT:
                return sendNotificationSignat(certificateId);
            case KFSIGN:
                return sendNotificationKfsign(certificateId);
            case RADERA:
                return sendNotificationDelete(certificateId);
            case MAKULE:
                return sendNotificationRevoke(certificateId);
            case SKICKA:
                return sendNotificationForSend(certificateId);
            case NYFRFV:
                return sendNotificationForQuestionForCare(certificateId, true);
            case NYSVFM:
                return sendNotificationForAnswerFromRecipient(certificateId, true);
            case HANFRFV:
                return sendNotificationForHandledQuestionForCare(certificateId);
            case NYFRFM:
                return sendNotificationForQuestionFromRecipient(certificateId, true);
            case HANFRFM:
                return sendNotificationForHandledQuestionFromRecipient(certificateId);
            default:
                return "";
        }
    }

    private String sendNotificationForHandledQuestionFromRecipient(String certificateId) {
        final var messageId = sendNotificationForQuestionFromRecipient(certificateId, false);
        final var arendeFromFk = arendeRepository.findOneByMeddelandeId(messageId);
        arendeFromFk.setStatus(Status.CLOSED);
        arendeRepository.save(arendeFromFk);

        final var arende = new Arende();
        final var draft = utkastService.getDraft(certificateId, false);
        addDraftInfoToArende(arende, draft, true);
        addTimesToArende(arende, false);
        addMessageInfoToArende(arende, ArendeAmne.KONTKT, "WC", Status.CLOSED);
        addIdsToArende(arende, messageId);
        arendeRepository.save(arende);

        notificationService.sendNotificationForQAs(certificateId, NEW_ANSWER_FROM_CARE);
        return certificateId;
    }

    private String sendNotificationForQuestionFromRecipient(String certificateId, boolean sendNotification) {
        final var arende = new Arende();
        final var draft = utkastService.getDraft(certificateId, false);
        addDraftInfoToArende(arende, draft, false);
        addTimesToArende(arende, true);
        addMessageInfoToArende(arende, ArendeAmne.KONTKT, "FK", Status.PENDING_INTERNAL_ACTION);
        addIdsToArende(arende, null);
        arendeRepository.save(arende);

        if (sendNotification) {
            notificationService.sendNotificationForQuestionReceived(arende);
        }
        return arende.getMeddelandeId();
    }

    private String sendNotificationForHandledQuestionForCare(String certificateId) {
        final var messageId = sendNotificationForAnswerFromRecipient(certificateId, false);
        final var arende = arendeRepository.findOneByMeddelandeId(messageId);
        arende.setStatus(Status.CLOSED);
        arendeRepository.save(arende);

        notificationService.sendNotificationForQAs(certificateId, QUESTION_FROM_CARE_HANDLED);

        return certificateId;
    }

    private String sendNotificationForAnswerFromRecipient(String certificateId, boolean sendNotification) {
        final var messageId = sendNotificationForQuestionForCare(certificateId, false);
        final var arende = new Arende();
        final var draft = utkastService.getDraft(certificateId, false);
        addDraftInfoToArende(arende, draft, true);
        addTimesToArende(arende, false);
        addMessageInfoToArende(arende, ArendeAmne.AVSTMN, "FK", Status.ANSWERED);
        addIdsToArende(arende, messageId);
        arendeRepository.save(arende);

        if (sendNotification) {
            notificationService.sendNotificationForQAs(certificateId, NEW_ANSWER_FROM_RECIPIENT);
        }
        return messageId;
    }

    private String sendNotificationForQuestionForCare(String certificateId, boolean sendNotification) {
        final var arende = new Arende();
        final var draft = utkastService.getDraft(certificateId, false);
        addDraftInfoToArende(arende, draft, true);
        addTimesToArende(arende, false);
        addMessageInfoToArende(arende, ArendeAmne.AVSTMN, "WC", Status.PENDING_EXTERNAL_ACTION);
        addIdsToArende(arende, null);
        arendeRepository.save(arende);

        if (sendNotification) {
            notificationService.sendNotificationForQAs(certificateId, NotificationEvent.NEW_QUESTION_FROM_CARE);
        }
        return arende.getMeddelandeId();
    }

    private void addIdsToArende(Arende arende, String answerToId) {
        arende.setSvarPaReferens(null);
        arende.setSvarPaId(answerToId);
        arende.setPaminnelseMeddelandeId(null);
        arende.setReferensId(null);
    }

    private void addMessageInfoToArende(Arende arende, ArendeAmne amne, String sentBy, Status status) {
        arende.setAmne(amne);
        arende.setSkickatAv(sentBy);
        arende.setStatus(status);
        arende.setMeddelandeId(UUID.randomUUID().toString());
        arende.setRubrik(amne.name());
        arende.setMeddelande("Message text");
    }

    private void addTimesToArende(Arende arende, boolean setLastDate) {
        arende.setTimestamp(LocalDateTime.now());
        arende.setSkickatTidpunkt(LocalDateTime.now());
        arende.setSenasteHandelse(LocalDateTime.now());
        arende.setSistaDatumForSvar(setLastDate ? LocalDate.now().plusDays(2) : null);
    }

    private void addDraftInfoToArende(Arende arende, Utkast draft, boolean setVardAktor) {
        arende.setIntygsId(draft.getIntygsId());
        arende.setIntygTyp(draft.getIntygsTyp());
        arende.setEnhetId(draft.getEnhetsId());
        arende.setEnhetName(draft.getEnhetsNamn());
        arende.setVardgivareName(draft.getVardgivarId());
        arende.setSigneratAv(draft.getSkapadAv().getHsaId());
        arende.setSigneratAvName(draft.getSkapadAv().getNamn());
        arende.setPatientPersonId(draft.getPatientPersonnummer().getPersonnummer());
        arende.setVardaktorName(setVardAktor ? draft.getSkapadAv().getHsaId() : null);
    }

    private String sendNotificationForSend(String certificateId) {
        final var draft = utkastRepository.findById(certificateId).orElse(new Utkast());
        final var hoSPerson = certificateTestabilityUtil.getHoSPerson(draft.getSkapadAv().getHsaId(), draft.getEnhetsId());
        final var certificateRequest = new CreateCertificateRequestDTO();
        certificateRequest.setStatus(CertificateStatus.SIGNED);
        certificateRequest.setSent(true);
        certificateTestabilityUtil.updateCertificateWithRequestedStatus(certificateRequest, hoSPerson, draft);
        notificationService.sendNotificationForIntygSent(certificateId);
        return certificateId;
    }

    private String sendNotificationRevoke(String certificateId) {
        final var draft = utkastRepository.findById(certificateId).orElse(new Utkast());
        draft.setAterkalladDatum(LocalDateTime.now());
        utkastRepository.save(draft);
        notificationService.sendNotificationForDraftRevoked(draft);
        return certificateId;
    }

    private String sendNotificationDelete(String certificateId) {
        final var draft = utkastRepository.findById(certificateId).orElse(new Utkast());
        utkastService.deleteUnsignedDraft(certificateId, draft.getVersion());
        return certificateId;
    }

    private String sendNotificationKfsign(String certificateId) {
        final var draft = utkastRepository.findById(certificateId).orElse(new Utkast());
        final var hoSPerson = certificateTestabilityUtil.getHoSPerson(draft.getSkapadAv().getHsaId(), draft.getEnhetsId());
        final var certificateRequest = new CreateCertificateRequestDTO();
        certificateRequest.setStatus(CertificateStatus.UNSIGNED);
        certificateTestabilityUtil.updateCertificateWithRequestedStatus(certificateRequest, hoSPerson, draft);
        notificationService.sendNotificationForDraftReadyToSign(draft);
        return certificateId;
    }

    private String sendNotificationSignat(String certificateId) {
        final var draft = utkastRepository.findById(certificateId).orElse(new Utkast());
        final var hoSPerson = certificateTestabilityUtil.getHoSPerson(draft.getSkapadAv().getHsaId(), draft.getEnhetsId());
        final var certificateRequest = new CreateCertificateRequestDTO();
        certificateRequest.setStatus(CertificateStatus.SIGNED);
        certificateTestabilityUtil.updateCertificateWithRequestedStatus(certificateRequest, hoSPerson, draft);
        notificationService.sendNotificationForDraftSigned(draft);
        return certificateId;
    }

    private NotificationDeliveryStatusEnum getDeliveryStatus(String certificatId) {
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

    private void clearArendenForCertificateIds(List<String> certificateIds) {
        for (var certificateId : certificateIds) {
            final var arenden = arendeRepository.findByIntygsId(certificateId);
            arenden.forEach(arende -> arendeRepository.delete(arende));
        }
    }
}
