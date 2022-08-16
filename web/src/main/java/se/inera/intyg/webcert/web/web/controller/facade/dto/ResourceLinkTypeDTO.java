/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.facade.dto;

public enum ResourceLinkTypeDTO {
    EDIT_CERTIFICATE,
    READ_CERTIFICATE,
    REMOVE_CERTIFICATE,
    FORWARD_CERTIFICATE,
    READY_FOR_SIGN,
    SIGN_CERTIFICATE,
    SEND_CERTIFICATE,
    REVOKE_CERTIFICATE,
    REPLACE_CERTIFICATE,
    REPLACE_CERTIFICATE_CONTINUE,
    RENEW_CERTIFICATE,
    PRINT_CERTIFICATE,
    COPY_CERTIFICATE,
    COPY_CERTIFICATE_CONTINUE,
    FMB,
    QUESTIONS,
    QUESTIONS_NOT_AVAILABLE,
    CREATE_QUESTIONS,
    ANSWER_QUESTION,
    HANDLE_QUESTION,
    COMPLEMENT_CERTIFICATE,
    CANNOT_COMPLEMENT_CERTIFICATE,
    CREATE_CERTIFICATE_FROM_TEMPLATE,
    CREATE_CERTIFICATE_FROM_CANDIDATE,
    ACCESS_SEARCH_CREATE_PAGE,
    ACCESS_DRAFT_LIST,
    ACCESS_QUESTION_LIST,
    ACCESS_SIGNED_CERTIFICATES_LIST,
    LOG_OUT,
    CREATE_CERTIFICATE,
    CHOOSE_UNIT,
    CHANGE_UNIT,
    PRIVATE_PRACTITIONER_PORTAL,
    NAVIGATE_BACK_BUTTON,
    WARNING_NORMAL_ORIGIN,
}
