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
package se.inera.intyg.webcert.notification_sender.notifications.services;

import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.CORRELATION_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.HANDELSE;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYGS_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.LOGISK_ADRESS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Message;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing.NotificationPostProcessingService;

@Slf4j
@Component
public class NotificationPostProcessor {

    @Autowired
    private MdcHelper mdcHelper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private NotificationPostProcessingService notificationPostProcessingService;

    public void process(Message message) {
        try {
            MDC.put(MdcLogConstants.TRACE_ID_KEY, mdcHelper.traceId());
            MDC.put(MdcLogConstants.SPAN_ID_KEY, mdcHelper.spanId());
            MDC.put(MdcLogConstants.EVENT_CERTIFICATE_ID, message.getHeader(INTYGS_ID).toString());
            MDC.put(MdcLogConstants.EVENT_STATUS_UPDATE_EVENT_ID, message.getHeader(HANDELSE).toString());
            MDC.put(MdcLogConstants.EVENT_LOGICAL_ADDRESS, message.getHeader(LOGISK_ADRESS).toString());
            MDC.put(MdcLogConstants.EVENT_STATUS_UPDATE_CORRELATION_ID, message.getHeader(CORRELATION_ID).toString());

            final var resultMessage = getNotificationResultMessage(message);
            notificationPostProcessingService.processNotificationResult(resultMessage);
        } catch (JsonProcessingException e) {
            log.error(getLogErrorMessage(message), e);
        } finally {
            MDC.clear();
        }
    }

    private NotificationResultMessage getNotificationResultMessage(Message message) throws JsonProcessingException {
        return objectMapper.readValue(message.getBody(String.class), NotificationResultMessage.class);
    }

    private String getLogErrorMessage(Message message) {
        return String.format("Failure to process NotificationWSResultMessage [certificateId: %s, correlationId: %s, "
                + "logicalAddress: %s, event: %s]", message.getHeader(INTYGS_ID), message.getHeader(CORRELATION_ID),
            message.getHeader(LOGISK_ADRESS), message.getHeader(HANDELSE));
    }
}
