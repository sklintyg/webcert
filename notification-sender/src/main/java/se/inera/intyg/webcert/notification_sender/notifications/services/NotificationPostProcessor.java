/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notification_sender.notifications.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationResend;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationResendRepository;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;

public class NotificationPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationPostProcessor.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HandelseRepository handelseRepo;

    @Autowired
    private NotificationResendRepository notificationResendRepository;

    private static final String NOTIFICATION_RESULT_RESEND = "RESEND";
    private static final String NOTIFICATION_RESULT_FAILURE = "FAILURE";
    private static final String NOTIFICATION_RESULT_SUCCESS = "SUCCESS";

    public void process(Exchange exchange) {

        String notificationResult = exchange.getMessage().getHeader(NotificationRouteHeaders.NOTIFICATION_RESULT, String.class);
        String correlationId = exchange.getMessage().getHeader(NotificationRouteHeaders.CORRELATION_ID, String.class);
        String timestamp = exchange.getMessage().getHeader(Constants.JMS_TIMESTAMP, String.class);
        String certificateId = exchange.getMessage().getHeader(NotificationRouteHeaders.INTYGS_ID, String.class);
        String eventType = exchange.getMessage().getHeader(NotificationRouteHeaders.HANDELSE, String.class);
        String messageJson = exchange.getMessage().getBody(String.class);

        // TODO Throughout, check what error handling is needed, add logging and monitorlogging
        try {
            NotificationResend existingResendRecord = notificationResendRepository.findByCorrelationId(correlationId).orElse(null);
            CertificateStatusUpdateForCareType message = notificationMessageFromJson(messageJson, certificateId, eventType, timestamp);
            Handelse event = createEvent(message, notificationResult);
            handleNotificationResult(event, existingResendRecord, notificationResult, correlationId);
        } catch (Exception e) {
            LOG.error("Notification postprocessing failed: {}", e.getMessage());
        }
    }

    private void handleNotificationResult(Handelse event, NotificationResend existingResendRecord, String notificationResult,
        String correlationId) {

        if (existingResendRecord == null) {
            Handelse persistedEvent = handelseRepo.save(event);

            if (notificationResult.equals(NOTIFICATION_RESULT_RESEND)) {
                NotificationResend newResendRecord = new NotificationResend(correlationId, persistedEvent.getId(), "STANDARD",
                    LocalDateTime.now().plusMinutes(1), 0);
                notificationResendRepository.save(newResendRecord);
            }
        } else {
            updateExistingEvent(existingResendRecord, notificationResult);

            if (notificationResult.equals(NOTIFICATION_RESULT_RESEND)) {
                NotificationResend updatedResendRecord = updateExistingResendRecord(existingResendRecord);
                notificationResendRepository.save(updatedResendRecord);
            } else {
                notificationResendRepository.delete(existingResendRecord);
            }
        }
    }

    private Boolean updateExistingEvent(NotificationResend existingResendRecord, String deliveryStatus) {
        Handelse eventToUpdate = handelseRepo.findById(existingResendRecord.getEventId()).orElse(null);
        if (eventToUpdate != null) {
            eventToUpdate.setDeliveryStatus(deliveryStatus);
            handelseRepo.save(eventToUpdate);
            return true;
        }
        return false;
    }

    private NotificationResend updateExistingResendRecord(NotificationResend existingResendRecord) {
        // TODO Improve by using a NotificationResendStrategy for determining resend times and max redeliveries etc.
        int nextResendAttempt = existingResendRecord.getResendAttempts() + 1;
        existingResendRecord.setResendAttempts(nextResendAttempt);
        existingResendRecord.setResendTime(LocalDateTime.now().plusMinutes(nextResendAttempt * 2));
        return existingResendRecord;
    }

    private CertificateStatusUpdateForCareType notificationMessageFromJson(String message, String certificateId,
        String eventType, String timestamp) {
        try {
            return objectMapper.readValue(message, CertificateStatusUpdateForCareType.class);
        } catch (JsonProcessingException e) {
            LOG.error("Exception occurred when extracting notification json message for certificate id {}, event {} and timestamp {} "
                + "with error message: {}",
                certificateId, eventType, timestamp, e.getMessage());
            return null;
        }
    }

    private Handelse createEvent(CertificateStatusUpdateForCareType statusUpdateMessage, String notificationResult) {

        Amneskod topicCode = statusUpdateMessage.getHandelse().getAmne();

        Handelse event = new Handelse();
        event.setCode(HandelsekodEnum.fromValue(statusUpdateMessage.getHandelse().getHandelsekod().getCode()));
        event.setEnhetsId(statusUpdateMessage.getIntyg().getSkapadAv().getEnhet().getEnhetsId().getExtension());
        event.setIntygsId(statusUpdateMessage.getIntyg().getIntygsId().getExtension());
        event.setPersonnummer(statusUpdateMessage.getIntyg().getPatient().getPersonId().getExtension());
        event.setTimestamp(statusUpdateMessage.getHandelse().getTidpunkt());
        event.setVardgivarId(statusUpdateMessage.getIntyg().getSkapadAv().getEnhet().getVardgivare().getVardgivareId().getExtension());
        event.setAmne(topicCode != null ? ArendeAmne.valueOf(topicCode.getCode()) : null);
        event.setSistaDatumForSvar(statusUpdateMessage.getHandelse().getSistaDatumForSvar());
        event.setHanteratAv(statusUpdateMessage.getHanteratAv().getExtension());
        event.setDeliveryStatus(notificationResult);

        return event;
    }
}
