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
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.TECHNICAL_ERROR;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.ERROR;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.INFO;
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
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.ExceptionInfoMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum;

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
            NotificationResultMessage resultMessage = objectMapper.readValue(message.getBody(String.class),
                NotificationResultMessage.class);
            processNotificationResult(resultMessage);
        } catch (JsonProcessingException e) {
            LOG.error(getLogErrorMessage(message), e);
            // TODO add monitorlog
            //  is recovery possible? (should we try to resend, check resend table etc)
        }
    }

    private void processNotificationResult(NotificationResultMessage resultMessage) {

        NotificationDeliveryStatusEnum deliveryStatus = extractDeliveryStatusFromResult(resultMessage);
        // TODO: Why changing the event here?
        resultMessage.getEvent().setDeliveryStatus(deliveryStatus);

        switch (deliveryStatus) {
            case SUCCESS:
                notificationRedeliveryService.handleNotificationSuccess(resultMessage);
                break;
            case RESEND:
                notificationRedeliveryService.handleNotificationResend(resultMessage);
                break;
            case FAILURE:
                notificationRedeliveryService.handleNotificationFailure(resultMessage);
        }
    }

    private NotificationDeliveryStatusEnum extractDeliveryStatusFromResult(NotificationResultMessage notificationResult) {
        ExceptionInfoMessage exceptionInfoMessage = notificationResult.getExceptionInfoMessage();

        if (Objects.nonNull(exceptionInfoMessage)) {
            return getDeliveryStatusOnException(exceptionInfoMessage, notificationResult);
        } else {
            NotificationResultType resultType = notificationResult.getResultType();
            return getDeliveryStatusOnResultType(resultType, notificationResult);
        }
    }

    private NotificationDeliveryStatusEnum getDeliveryStatusOnException(ExceptionInfoMessage exceptionInfoMessage,
        NotificationResultMessage resultMessage) {
        final String exceptionType = exceptionInfoMessage.getExceptionType();
        final String exceptionMessage = exceptionInfoMessage.getExceptionMessage();
        if (SOAPFAULTEXCEPTION.equals(exceptionType) && Objects.nonNull(exceptionMessage) && (exceptionMessage.contains(MARSHALLING_ERROR)
            || exceptionMessage.contains(UNMARSHALLING_ERROR))) {
            LOG.error("Failure sending status update to care for {}, {}", resultMessage, exceptionInfoMessage.getStackTrace());
            return FAILURE;
        } else {
            LOG.warn("Failure sending status update to care for {}, {}, Attempting redelivery...", resultMessage,
                exceptionInfoMessage.getStackTrace());
            return RESEND;
        }
    }

    private NotificationDeliveryStatusEnum getDeliveryStatusOnResultType(NotificationResultType resultType,
        NotificationResultMessage resultMessage) {
        NotificationResultTypeEnum result = resultType.getNotificationResult();
        if (ERROR == result) {
            return getDeliveryStatusOnError(resultType, resultMessage);
        }
        if (INFO == result) {
            LOG.info("Received info message from care for status update {}, info received: {}", resultMessage,
                resultType.getNotificationResultText());
        }
        return SUCCESS;
    }

    private NotificationDeliveryStatusEnum getDeliveryStatusOnError(NotificationResultType resultType,
        NotificationResultMessage resultMessage) {

        NotificationErrorTypeEnum errorType = resultType.getNotificationErrorType();
        String errorText = resultType.getNotificationResultText();
        if (errorType == TECHNICAL_ERROR) {
            LOG.warn("{} returned from care for status update {}, Error message: {}, Attempting redelivery...", errorType, resultMessage,
                errorText);
            return RESEND;
        } else {
            LOG.error("{} returned from care for status update {}, Error message: {}", errorType, resultMessage, errorText);
            return FAILURE;
        }
    }

    private String getLogErrorMessage(Message message) {
        return String.format("Failure mapping json to NotificationWSResultMessage [certificateId: %s, correlationId: %s, "
            + "logicalAddress: %s, event: %s]", message.getHeader(INTYGS_ID), message.getHeader(CORRELATION_ID),
            message.getHeader(LOGISK_ADRESS), message.getHeader(HANDELSE));
    }
}
