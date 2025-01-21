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
package se.inera.intyg.webcert.notification_sender.notifications.dto;

import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum;

public final class NotificationResultType {

    private NotificationResultTypeEnum notificationResult;
    private String exception;
    private String notificationResultText;
    private NotificationErrorTypeEnum notificationErrorType;

    public NotificationResultType() {

    }

    public NotificationResultTypeEnum getNotificationResult() {
        return notificationResult;
    }

    public void setNotificationResult(NotificationResultTypeEnum notificationResult) {
        this.notificationResult = notificationResult;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getNotificationResultText() {
        return notificationResultText;
    }

    public void setNotificationResultText(String notificationResultText) {
        this.notificationResultText = notificationResultText;
    }

    public NotificationErrorTypeEnum getNotificationErrorType() {
        return notificationErrorType;
    }

    public void setNotificationErrorType(NotificationErrorTypeEnum notificationErrorType) {
        this.notificationErrorType = notificationErrorType;
    }
}
