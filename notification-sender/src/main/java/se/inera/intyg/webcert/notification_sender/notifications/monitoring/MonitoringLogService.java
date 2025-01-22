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
package se.inera.intyg.webcert.notification_sender.notifications.monitoring;

import java.time.LocalDateTime;

public interface MonitoringLogService {

    void logStatusUpdateForCareStatusSuccess(long eventId, String eventType, String certificateId, String correlationId, String unitId,
        int sendAttempt);

    //CHECKSTYLE:OFF ParameterNumber
    void logStatusUpdateForCareStatusResend(long eventId, String eventType, String unitId, String certificateId, String correlationId,
        String errorCode, String message, int sendAttempt, LocalDateTime nextAttempt);
    //CHECKSTYLE:ON ParameterNumber

    //CHECKSTYLE:OFF ParameterNumber
    void logStatusUpdateForCareStatusFailure(long eventId, String eventType, String unitId, String certificateId, String correlationId,
        String errorCode, String message, int sendAttempt);
    //CHECKSTYLE:ON ParameterNumber

}
