/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.integration.analytics.model;

public enum CertificateAnalyticsMessageType {
    DRAFT_CREATED,
    DRAFT_CREATED_WITH_PREFILL,
    DRAFT_DELETED,
    DRAFT_UPDATED,
    DRAFT_READY_FOR_SIGN,
    DRAFT_CREATED_FROM_TEMPLATE,
    LOCKED_DRAFT_REVOKED,
    CERTIFICATE_SIGNED,
    CERTIFICATE_SENT,
    CERTIFICATE_RENEWED,
    CERTIFICATE_REPLACED,
    CERTIFICATE_COMPLEMENTED,
    CERTIFICATE_REVOKED,
    CERTIFICATE_PRINTED,
    QUESTION_TO_RECIPIENT,
    ANSWER_TO_RECIPIENT,
    QUESTION_FROM_RECIPIENT,
    ANSWER_FROM_RECIPIENT,
    REMINDER_FROM_RECIPIENT,
    COMPLEMENT_FROM_RECIPIENT
}
