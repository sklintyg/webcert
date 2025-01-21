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
package se.inera.intyg.webcert.web.service.access;

/**
 * Enum that defines different results when access is checked. If NO_PROBLEM is returned,
 * the user is considered having access. If any other code is returned, no access is given and
 * the code explains the reason for the restriction.
 */
public enum AccessResultCode {

    /**
     * Has access.
     */
    NO_PROBLEM,

    /**
     * No access, could not check if patient has sekretess or not due to unavailable PersonUppgiftsTjänst.
     */
    PU_PROBLEM,

    /**
     * No access. Missing privilege to handle patient with sekretess.
     */
    AUTHORIZATION_SEKRETESS,

    /**
     * No access. Not allowed to handle patient with sekretess on another CareUnit than the user is logged in to.
     */
    AUTHORIZATION_SEKRETESS_UNIT,

    /**
     * No Access. Not allowed to handle patient on another CareUnit than the user is logged in to.
     */
    AUTHORIZATION_DIFFERENT_UNIT,

    /**
     * No Access. Not allowed to create new draft due to Unique draft feature.
     */
    UNIQUE_DRAFT,

    /**
     * No Access. Not allowed to create new draft due to Unique certificate feature.
     */
    UNIQUE_CERTIFICATE,

    /**
     * No Access. Not allowed to handle deceased patient.
     */
    DECEASED_PATIENT,

    /**
     * No Access. Not allowed to handle patient when logged in with inactive unit parameter.
     */
    INACTIVE_UNIT,

    /**
     * No Access. Not allowed to handle patient when logged in with renew false parameter.
     */
    RENEW_FALSE,

    /**
     * No Access. Not allowed due to feature or privilege missing.
     */
    AUTHORIZATION_VALIDATION,

    /**
     * No access. Not allowed to perform action when Patient is Test indicated.
     */
    TEST_INDICATED_PATIENT,

    /**
     * No access. Not allowed to perform action when certificate is flagged as a test certificate.
     */
    TEST_CERTIFICATE,

    /**
     * No access. Not allowed to perform action because it is blocked.
     */
    AUTHORIZATION_BLOCKED,

    /**
     * No access. Not allowed to perform action because not the latest version.
     */
    NOT_LATEST_MAJOR_VERSION,

    /**
     * No access. Not allowed to perform action because selected care provider has no subscription.
     */
    MISSING_SUBSCRIPTION,
    /**
     * No access. Not allowed to perform action because the certificate type is considered inactive
     */
    INACTIVE_CERTIFICATE_TYPE,

}
