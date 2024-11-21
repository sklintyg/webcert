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
package se.inera.intyg.webcert.notification_sender.notifications.monitoring;


import java.time.LocalDateTime;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.logging.LogMarkers;
import se.inera.intyg.webcert.logging.MdcCloseableMap;
import se.inera.intyg.webcert.logging.MdcLogConstants;

@Service("notificationMonitoringLogService")
@Slf4j
public class MonitoringLogServiceImpl implements MonitoringLogService {

    @Override
    public void logStatusUpdateForCareStatusSuccess(long eventId, String eventType, String certificateId, String correlationId,
        String unitId, int sendAttempt) {
        try (MdcCloseableMap ignored = setMdc(eventId, eventType, unitId, certificateId, correlationId, sendAttempt)
            .put(MdcLogConstants.EVENT_ACTION, toEventType(MonitoringEvent.STATUS_UPDATE_RESULT_SUCCESS))
            .build()
        ) {
            logEvent(MonitoringEvent.STATUS_UPDATE_RESULT_SUCCESS, sendAttempt, certificateId, correlationId, unitId, eventId,
                eventType);
        }
    }

    @Override //CHECKSTYLE:OFF ParameterNumber
    public void logStatusUpdateForCareStatusResend(long eventId, String eventType, String unitId, String certificateId,
        String correlationId, String errorCode, String message, int sendAttempt, LocalDateTime nextAttempt) {
        try (MdcCloseableMap ignored = setMdc(eventId, eventType, unitId, certificateId, correlationId, sendAttempt)
            .put(MdcLogConstants.EVENT_ACTION, toEventType(MonitoringEvent.STATUS_UPDATE_RESULT_RESEND))
            .put(MdcLogConstants.ERROR_CODE, errorCode)
            .put(MdcLogConstants.ERROR_MESSAGE, message)
            .build()
        ) {
            logEvent(MonitoringEvent.STATUS_UPDATE_RESULT_RESEND, sendAttempt, certificateId, correlationId, unitId, eventId, eventType,
                errorCode, message, nextAttempt);
        }
    } //CHECKSTYLE:ON ParameterNumber

    @Override //CHECKSTYLE:OFF ParameterNumber
    public void logStatusUpdateForCareStatusFailure(long eventId, String eventType, String unitId, String certificateId,
        String correlationId, String errorCode, String message, int sendAttempt) {
        try (MdcCloseableMap ignored = setMdc(eventId, eventType, unitId, certificateId, correlationId, sendAttempt)
            .put(MdcLogConstants.EVENT_ACTION, toEventType(MonitoringEvent.STATUS_UPDATE_RESULT_FAILURE))
            .put(MdcLogConstants.ERROR_CODE, errorCode)
            .put(MdcLogConstants.ERROR_MESSAGE, message)
            .build()
        ) {
            logEvent(MonitoringEvent.STATUS_UPDATE_RESULT_FAILURE, sendAttempt, certificateId, correlationId, unitId, eventId, eventType,
                errorCode, message);
        }
    } //CHECKSTYLE:ON ParameterNumber

    private MdcCloseableMap.Builder setMdc(long eventId, String eventType, String unitId, String certificateId,
        String correlationId, int sendAttempt) {
        return MdcCloseableMap.builder()
            .put(MdcLogConstants.EVENT_TYPE, MdcLogConstants.EVENT_TYPE_INFO)
            .put(MdcLogConstants.EVENT_STATUS_UPDATE_EVENT_ID, Long.toString(eventId))
            .put(MdcLogConstants.EVENT_STATUS_UPDATE_TYPE, eventType)
            .put(MdcLogConstants.EVENT_CERTIFICATE_UNIT_ID, unitId)
            .put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId)
            .put(MdcLogConstants.EVENT_STATUS_UPDATE_CORRELATION_ID, correlationId)
            .put(MdcLogConstants.EVENT_STATUS_UPDATE_SEND_ATTEMPT, Integer.toString(sendAttempt));
    }

    private void logEvent(MonitoringEvent logEvent, Object... logMsgArgs) {
        final var logMessage = "%s %s".formatted(logEvent.name(), logEvent.getMessage());
        log.info(LogMarkers.MONITORING, logMessage, logMsgArgs);
    }

    private String toEventType(MonitoringEvent monitoringEvent) {
        return monitoringEvent.name().toLowerCase().replace("_", "-");
    }


    @Getter
    private enum MonitoringEvent {
        STATUS_UPDATE_RESULT_SUCCESS(
            "Status update for care success on delivery attempt '{}' for event [certificateId: '{}', correlationId: '{}', "
                + "logicalAddress: '{}', eventId: '{}', eventType: '{}']."),
        STATUS_UPDATE_RESULT_RESEND(
            "Status update for care failure on delivery attempt '{}' for event [certificateId: '{}', correlationId: '{}', "
                + "logicalAddress: '{}', eventId: '{}', eventType: '{}', errorCode: '{}', errorMessage: '{}']. "
                + "Redelivery has been scheduled for '{}'."),
        STATUS_UPDATE_RESULT_FAILURE(
            "Status update for care failure on delivery attempt '{}' for event [certificateId: '{}', correlationId: '{}', "
                + "logicalAddress: '{}', eventId: '{}', eventType: '{}', errorCode: '{}', errorMessage: '{}']. "
                + "No further delivery attempts will be performed.");

        private final String message;

        MonitoringEvent(String msg) {
            this.message = msg;
        }
    }

}
