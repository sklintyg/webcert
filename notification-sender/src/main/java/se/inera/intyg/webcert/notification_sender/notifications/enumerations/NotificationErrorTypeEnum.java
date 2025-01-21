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
package se.inera.intyg.webcert.notification_sender.notifications.enumerations;

import java.util.stream.Stream;

public enum NotificationErrorTypeEnum {

    TECHNICAL_ERROR("TECHNICAL_ERROR", "Notification error type TECHNICAL_ERROR"),
    APPLICATION_ERROR("APPLICATION_ERROR", "Notification error type APPLICATION_ERROR"),
    VALIDATION_ERROR("VALIDATION_ERROR", "Notification error type VALIDATION_ERROR"),
    REVOKED("REVOKED", "Notification error type REVOKED"),
    WEBCERT_EXCEPTION("WEBCERT_EXCEPTION", "Notification error type WEBCERT_EXCEPTION");

    private final String value;
    private final String description;

    NotificationErrorTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String value() {
        return this.value;
    }

    public String description() {
        return this.description;
    }

    public static NotificationErrorTypeEnum fromValue(String value) {
        return Stream.of(values()).filter((s) -> value.equals(s.value())).findFirst()
            .orElseThrow(() -> new IllegalArgumentException(value));
    }
}
