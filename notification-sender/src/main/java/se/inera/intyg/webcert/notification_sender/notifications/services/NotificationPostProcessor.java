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

import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.FAILURE;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.RESEND;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.SUCCESS;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.CORRELATION_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.HANDELSE;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYGS_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.LOGISK_ADRESS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.ExceptionInfoTransporter;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationWSResultMessage;
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

    private static final String SOAPFAULTEXCEPTION = "javax.xml.ws.soap.SOAPFaultException";
    private static final String MARSHALLING_ERROR = "Marshalling Error";
    private static final String UNMARSHALLING_ERROR = "Unmarshalling Error";


    public void process(Message message) {

        try {
            NotificationWSResultMessage notificationWSMessage = objectMapper.readValue(message.getBody(String.class),
                NotificationWSResultMessage.class);
            processNotificationResult(notificationWSMessage);
        } catch (JsonProcessingException e) {
            LOG.error(getLogErrorMessage(message), e);
            // TODO add monitorlog
            //  is recovery possible? (should we try to resend, check resend table etc)
        }
    }

    private void processNotificationResult(NotificationWSResultMessage notificationResult) {

        NotificationWSResultMessage updatedNotificationResult = extractDeliveryStatusFromResult(notificationResult);

        // TOGGLE ON TESTING/DEMO
        // updatedNotificationResult = preparingForDemo(updatedNotificationResult);
        // #################################################################

        Handelse event = extractEventFromStatusUpdate(updatedNotificationResult.getStatusUpdate(),
            updatedNotificationResult.getDeliveryStatus());

        switch (updatedNotificationResult.getDeliveryStatus()) {
            case SUCCESS:
                notificationRedeliveryService.handleNotificationSuccess(updatedNotificationResult, event);
                break;
            case RESEND:
                notificationRedeliveryService.handleNotificationResend(updatedNotificationResult, event);
                break;
            case FAILURE:
                notificationRedeliveryService.handleNotificationFailure(updatedNotificationResult, event);
        }
    }

    private NotificationWSResultMessage preparingForDemo(NotificationWSResultMessage demoMessage) {

        if (demoMessage.getStatusUpdate().getHandelse().getHandelsekod().getCode().equals("ANDRAT")) {
            demoMessage.setDeliveryStatus(RESEND);
            //ResultType resultType = new ResultType();
            //resultType.setErrorId(ErrorIdType.VALIDATION_ERROR);
            //resultType.setResultText("Fel, fel FEL!");
            //demoMessage.setResultType(resultType);
        }
        return demoMessage;
    }

    private NotificationWSResultMessage extractDeliveryStatusFromResult(NotificationWSResultMessage notificationResult) {
        ExceptionInfoTransporter exceptionInfoTransporter = notificationResult.getExceptionInfoTransporter();

        if (Objects.nonNull(exceptionInfoTransporter)) {
            NotificationDeliveryStatusEnum deliveryStatusOnException = getDeliveryStatusOnException(exceptionInfoTransporter,
                notificationResult);
            notificationResult.setDeliveryStatus(deliveryStatusOnException);
        } else {
            ResultType resultType = notificationResult.getResultType();
            NotificationDeliveryStatusEnum deliveryStatusOnResultType = getDeliveryStatusOnResultType(resultType, notificationResult);
            notificationResult.setDeliveryStatus(deliveryStatusOnResultType);
        }
        return notificationResult;
    }

    private NotificationDeliveryStatusEnum getDeliveryStatusOnException(ExceptionInfoTransporter exceptionInfoTransporter,
        NotificationWSResultMessage message) {
        final String exceptionType = exceptionInfoTransporter.getExceptionType();
        final String exceptionMessage = exceptionInfoTransporter.getExceptionMessage();
        if (SOAPFAULTEXCEPTION.equals(exceptionType) && Objects.nonNull(exceptionMessage) && (exceptionMessage.contains(MARSHALLING_ERROR)
            || exceptionMessage.contains(UNMARSHALLING_ERROR))) {
            LOG.error("Failure sending status update to care for {}, {}", message, exceptionInfoTransporter.getStackTrace());
            return FAILURE;
        } else {
            LOG.warn("Failure sending status update to care for {}, {}, Attempting redelivery...", message,
                exceptionInfoTransporter.getStackTrace());
            return RESEND;
        }
    }

    private NotificationDeliveryStatusEnum getDeliveryStatusOnResultType(ResultType resultType,
        NotificationWSResultMessage notificationResult) {
        ResultCodeType resultCode = resultType.getResultCode();
        if (ResultCodeType.ERROR == resultCode) {
            return getDeliveryStatusOnError(resultType, notificationResult);
        }
        if (ResultCodeType.INFO == resultCode) {
            LOG.info("Received info message from care for status update {}, info received: {}", notificationResult,
                resultType.getResultText());
        }
        return SUCCESS;
    }

    private NotificationDeliveryStatusEnum getDeliveryStatusOnError(ResultType resultType, NotificationWSResultMessage resultMessage) {
        ErrorIdType errorId = resultType.getErrorId();
        String errorText = resultType.getResultText();
        if (errorId == ErrorIdType.TECHNICAL_ERROR) {
            LOG.warn("{} returned from care for status update {}, Error message: {}, Attempting redelivery...", errorId, resultMessage,
                errorText);
            return RESEND;
        } else {
            LOG.error("{} returned from care for status update {}, Error message: {}", errorId, resultMessage, errorText);
            return FAILURE;
        }
    }

    private Handelse extractEventFromStatusUpdate(CertificateStatusUpdateForCareType statusUpdateMessage,
        NotificationDeliveryStatusEnum deliveryStatus) {
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
        event.setDeliveryStatus(deliveryStatus);
        return event;
    }

    private String getLogErrorMessage(Message message) {
        return String.format("Failure mapping json to NotificationWSResultMessage [certificateId: %s, correlationId: %s, "
            + "logicalAddress: %s, event: %s]", message.getHeader(INTYGS_ID), message.getHeader(CORRELATION_ID),
            message.getHeader(LOGISK_ADRESS), message.getHeader(HANDELSE));
    }
}
