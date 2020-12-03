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

package se.inera.intyg.webcert.notification_sender.notifications.monitoring;


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
    public void logStatusUpdateForCareStatusOk(String hanType, String unitId, String certificateId) {
        logEvent(MonitoringEvent.STATUS_UPDATE_RESULT_OK, hanType, unitId, certificateId);
    }

    @Override
    public void logStatusUpdateForCareStatusResend(String hanType, String unitId, String certificateId) {
        logEvent(MonitoringEvent.STATUS_UPDATE_RESULT_RESEND, hanType, unitId, certificateId);
    }

    @Override
    public void logStatusUpdateForCareStatusFailure(String hanType, String unitId, String certificateId) {
        logEvent(MonitoringEvent.STATUS_UPDATE_RESULT_FAILURE, hanType, unitId, certificateId);
    }

    private void logEvent(MonitoringEvent logEvent, Object... logMsgArgs) {

        StringBuilder logMsg = new StringBuilder();
        logMsg.append(logEvent.name()).append(SPACE).append(logEvent.getMessage());

        LOG.info(LogMarkers.MONITORING, logMsg.toString(), logMsgArgs);
    }

    private enum MonitoringEvent {

        STATUS_UPDATE_RESULT_OK("Status update for care message '{}' successfully delivered to unit '{}' for '{}'"),
        STATUS_UPDATE_RESULT_RESEND("Status update for care message '{}' failed deliver to unit '{}' for '{}'. Will resend."),
        STATUS_UPDATE_RESULT_FAILURE(
            "Status update for care message '{}' failed deliver to unit '{}' for '{}'. Will not attempt to redeliver.");

        private final String message;

        MonitoringEvent(String msg) {
            this.message = msg;
        }

        public String getMessage() {
            return message;
        }
    }
}
