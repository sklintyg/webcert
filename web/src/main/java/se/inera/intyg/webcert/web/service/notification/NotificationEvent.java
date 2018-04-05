/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.notification;

public enum NotificationEvent {
    NEW_QUESTION_FROM_RECIPIENT,
    NEW_QUESTION_FROM_CARE,
    NEW_ANSWER_FROM_RECIPIENT,
    NEW_ANSWER_FROM_CARE,
    QUESTION_FROM_RECIPIENT_HANDLED,
    QUESTION_FROM_RECIPIENT_UNHANDLED,
    QUESTION_FROM_CARE_HANDLED,
    QUESTION_FROM_CARE_UNHANDLED,
    QUESTION_FROM_CARE_WITH_ANSWER_HANDLED,
    QUESTION_FROM_CARE_WITH_ANSWER_UNHANDLED;
}
