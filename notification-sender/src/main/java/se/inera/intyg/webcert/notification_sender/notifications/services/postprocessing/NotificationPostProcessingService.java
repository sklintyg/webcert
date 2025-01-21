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
package se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing;

import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.FAILURE;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.RESEND;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.SUCCESS;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.TECHNICAL_ERROR;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.WEBCERT_EXCEPTION;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.ERROR;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.INFO;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.UNRECOVERABLE_ERROR;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum;

@Service
public class NotificationPostProcessingService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationPostProcessingService.class);

    private static final String SOAPFAULTEXCEPTION = "jakarta.xml.ws.soap.SOAPFaultException";
    private static final String MARSHALLING_ERROR = "Marshalling Error";
    private static final String UNMARSHALLING_ERROR = "Unmarshalling Error";

    @Autowired
    private NotificationResultSuccessService notificationResultSuccessService;

    @Autowired
    private NotificationResultFailedService notificationResultFailedService;

    @Autowired
    private NotificationResultResendService notificationResultResendService;

    public void processNotificationResult(NotificationResultMessage resultMessage) {

        final var deliveryStatus = extractDeliveryStatusFromResult(resultMessage);
        resultMessage.getEvent().setDeliveryStatus(deliveryStatus);

        switch (deliveryStatus) {
            case SUCCESS:
                notificationResultSuccessService.process(resultMessage);
                break;
            case RESEND:
                notificationResultResendService.process(resultMessage);
                break;
            case FAILURE:
                notificationResultFailedService.process(resultMessage);
        }
    }

    private NotificationDeliveryStatusEnum extractDeliveryStatusFromResult(NotificationResultMessage resultMessage) {
        if (resultMessage.getResultType().getNotificationErrorType() == WEBCERT_EXCEPTION) {
            return getDeliveryStatusOnException(resultMessage);
        } else {
            return getDeliveryStatusOnResultType(resultMessage);
        }
    }

    private NotificationDeliveryStatusEnum getDeliveryStatusOnException(NotificationResultMessage resultMessage) {
        if (isUnrecoverableException(resultMessage.getResultType())) {
            LOG.debug("Failure sending status update to care for {}", resultMessage);
            return FAILURE;
        } else {
            LOG.debug("Failure sending status update to care for {}, Attempting redelivery...", resultMessage);
            return RESEND;
        }
    }

    private NotificationDeliveryStatusEnum getDeliveryStatusOnResultType(NotificationResultMessage resultMessage) {
        NotificationResultType resultType = resultMessage.getResultType();
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
            LOG.debug("{} returned from care for status update {}, Error message: {}, Attempting redelivery...", errorType, resultMessage,
                errorText);
            return RESEND;
        } else {
            LOG.debug("{} returned from care for status update {}, Error message: {}", errorType, resultMessage, errorText);
            return FAILURE;
        }
    }

    private boolean isUnrecoverableException(NotificationResultType resultType) {
        String resultText = resultType.getNotificationResultText();
        return resultType.getNotificationResult() == UNRECOVERABLE_ERROR
            || (SOAPFAULTEXCEPTION.equals(resultType.getException()) && Objects.nonNull(resultText)
            && (resultText.contains(MARSHALLING_ERROR) || resultText.contains(UNMARSHALLING_ERROR)));
    }
}
