/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

public final class NotificationResultType {

    private NotificationResultTypeEnum notificationResult;
    private String notificationResultText;
    private NotificationErrorTypeEnum notificationErrorType;

    public NotificationResultType() { }

    private NotificationResultType(NotificationResultTypeEnum notificationResult, String notificationResultText,
        NotificationErrorTypeEnum notificationErrorType) {
        this.notificationResult = notificationResult;
        this.notificationResultText = notificationResultText;
        this.notificationErrorType = notificationErrorType;
    }

    public NotificationResultTypeEnum getNotificationResult() {
        return notificationResult;
    }

    public void setNotificationResult(NotificationResultTypeEnum notificationResult) {
        this.notificationResult = notificationResult;
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

    @JsonIgnore
    public static NotificationResultType fromResultTypeV3(ResultType resultType) {
        NotificationResultTypeEnum notificationResult =
            resultType.getResultCode() != null ? NotificationResultTypeEnum.fromValue(resultType.getResultCode().value()) : null;
        NotificationErrorTypeEnum notificationError =
            resultType.getErrorId() != null ? NotificationErrorTypeEnum.fromValue(resultType.getErrorId().value()) : null;

        return new NotificationResultType(notificationResult, resultType.getResultText(), notificationError);
    }
}
