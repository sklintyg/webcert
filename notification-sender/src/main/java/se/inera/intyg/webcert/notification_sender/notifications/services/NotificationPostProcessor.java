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
import java.util.Objects;
import javax.xml.ws.soap.SOAPFaultException;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.NotificationWSResultMessage;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

public class NotificationPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationPostProcessor.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRedeliveryService notificationRedeliveryService;

    public enum NotificationResultEnum { SUCCESS, RESEND, FAILURE }

    public void process(Message message) {
        try {
            NotificationWSResultMessage notificationWSMessage = objectMapper.readValue(message.getBody(String.class),
                NotificationWSResultMessage.class);
            processNotificationResult(notificationWSMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void processNotificationResult(NotificationWSResultMessage notificationResult) {

        NotificationResultEnum deliveryStatus = extractDeliveryStatusFromResult(notificationResult);
        deliveryStatus = NotificationResultEnum.RESEND;
        CertificateStatusUpdateForCareType statusUpdateMessage = notificationResult.getStatusMessage();
        Handelse event = extractEventFromStatusUpdate(statusUpdateMessage, deliveryStatus);
        String correlationId = notificationResult.getCorrelationId();

        switch (deliveryStatus) {
            case SUCCESS :
                notificationRedeliveryService.handleNotificationSuccess(correlationId, event, deliveryStatus);
                break;
            case RESEND :
                notificationRedeliveryService.handleNotificationResend(correlationId, event, deliveryStatus, statusUpdateMessage);
                break;
            case FAILURE :
                notificationRedeliveryService.handleNotificationFailure(correlationId, event, deliveryStatus);
        }
    }

    private NotificationResultEnum extractDeliveryStatusFromResult(NotificationWSResultMessage notificationResult) {
        Exception exception = notificationResult.getException();
        ResultType resultType = notificationResult.getResultType();
        if (Objects.nonNull(exception)) {
            return getDeliveryStatusOnException(exception, notificationResult);
        } else {
            return getDeliveryStatusOnResultType(resultType, notificationResult);
        }
    }

    private NotificationResultEnum getDeliveryStatusOnResultType(ResultType resultType, NotificationWSResultMessage notificationResult) {
        ResultCodeType resultCode = resultType.getResultCode();
        if (ResultCodeType.ERROR == resultCode) {
            return getDeliveryStatusOnError(resultType, notificationResult);
        }
        if (ResultCodeType.INFO == resultCode) {
            LOG.info("Received info message from care for status update {}, info received: {}", notificationResult,
                resultType.getResultText());
        }
        return NotificationResultEnum.SUCCESS;
    }

    private NotificationResultEnum getDeliveryStatusOnError(ResultType resultType, NotificationWSResultMessage message) {
        ErrorIdType errorId = resultType.getErrorId();
        String errorText = resultType.getResultText();
        if (errorId == ErrorIdType.TECHNICAL_ERROR) {
            LOG.warn("{} returned from care for status update {}, Error message: {}, Attempting redelivery...", errorId, message,
                errorText);
            return NotificationResultEnum.RESEND;
        } else {
            LOG.error("{} returned from care for status update {}, Error message: {}", errorId, message, errorText);
            return NotificationResultEnum.FAILURE;
        }
    }

    private NotificationResultEnum getDeliveryStatusOnException(Exception exception, NotificationWSResultMessage message) {
        final String msg = exception.getMessage();
        if (exception instanceof SOAPFaultException && Objects.nonNull(msg) && (msg.contains("Marshalling Error")
            || msg.contains("Unmarshalling Error"))) {
            LOG.error("Failure sending status update to care for {}, {}", message, exception.getStackTrace());
            return NotificationResultEnum.FAILURE;
        } else {
            LOG.warn("Failure sending status update to care for {}, {}, Attempting redelivery...", message, exception.getStackTrace());
            return NotificationResultEnum.RESEND;
        }
    }

    private Handelse extractEventFromStatusUpdate(CertificateStatusUpdateForCareType statusUpdateMessage,
        NotificationResultEnum deliveryStatus) {
        Amneskod topicCode = statusUpdateMessage.getHandelse().getAmne();
        HsaId userId = statusUpdateMessage.getHanteratAv();

        Handelse event = new Handelse();
        event.setCode(HandelsekodEnum.fromValue(statusUpdateMessage.getHandelse().getHandelsekod().getCode()));
        event.setEnhetsId(statusUpdateMessage.getIntyg().getSkapadAv().getEnhet().getEnhetsId().getExtension());
        event.setIntygsId(statusUpdateMessage.getIntyg().getIntygsId().getExtension());
        event.setPersonnummer(statusUpdateMessage.getIntyg().getPatient().getPersonId().getExtension());
        event.setTimestamp(statusUpdateMessage.getHandelse().getTidpunkt());
        event.setVardgivarId(statusUpdateMessage.getIntyg().getSkapadAv().getEnhet().getVardgivare().getVardgivareId().getExtension());
        event.setAmne(topicCode != null ? ArendeAmne.valueOf(topicCode.getCode()) : null);
        event.setSistaDatumForSvar(statusUpdateMessage.getHandelse().getSistaDatumForSvar());
        event.setHanteratAv(userId != null ? userId.getExtension() : null);
        event.setDeliveryStatus(deliveryStatus.toString());

        return event;
    }
}