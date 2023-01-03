/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.monitoring.logging.LogMarkers;

/**
 * Interface used when logging to monitoring file. Used to ensure that the log entries are uniform and easy to parse.
 */
@Service("notificationMonitoringLogService")
public class MonitoringLogServiceImpl implements MonitoringLogService {

    private static final Object SPACE = " ";
    private static final Logger LOG = LoggerFactory.getLogger(MonitoringLogService.class);

    @Override
    public void logStatusUpdateForCareStatusSuccess(long eventId, String eventType, String certificateId, String correlationId,
        String unitId, int sendAttempt) {
        logEvent(MonitoringEvent.STATUS_UPDATE_RESULT_SUCCESS, sendAttempt, certificateId, correlationId, unitId, eventId,
            eventType);
    }

    @Override //CHECKSTYLE:OFF ParameterNumber
    public void logStatusUpdateForCareStatusResend(long eventId, String eventType, String unitId, String certificateId,
        String correlationId, String errorCode, String message, int sendAttempt, LocalDateTime nextAttempt) {
        logEvent(MonitoringEvent.STATUS_UPDATE_RESULT_RESEND, sendAttempt, certificateId, correlationId, unitId, eventId, eventType,
            errorCode, message, nextAttempt);
    } //CHECKSTYLE:ON ParameterNumber

    @Override //CHECKSTYLE:OFF ParameterNumber
    public void logStatusUpdateForCareStatusFailure(long eventId, String eventType, String unitId, String certificateId,
        String correlationId,
        String errorCode, String message, int sendAttempt) {
        logEvent(MonitoringEvent.STATUS_UPDATE_RESULT_FAILURE, sendAttempt, certificateId, correlationId, unitId, eventId, eventType,
            errorCode, message);
    } //CHECKSTYLE:ON ParameterNumber

    private void logEvent(MonitoringEvent logEvent, Object... logMsgArgs) {

        LOG.info(LogMarkers.MONITORING, logEvent.name() + SPACE + logEvent.getMessage(), logMsgArgs);
    }


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

        public String getMessage() {
            return message;
        }
    }
}
