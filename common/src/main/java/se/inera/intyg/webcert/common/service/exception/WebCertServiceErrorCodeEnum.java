/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.common.service.exception;

public enum WebCertServiceErrorCodeEnum {
    // @formatter:off

    INTERNAL_PROBLEM,                // Generic tech problem
    INVALID_STATE,                   // Operation not allowed at this state, probably because of concurrency issues

    INVALID_STATE_REPLACED,          // Operation not allowed at this state (because it's replaced). This error code is a
                                     // temporary fix until we can get relations for intyg from IT and therefore disable certain
                                     // actions in gui, (see INTYG-3619)

    AUTHORIZATION_PROBLEM,           // User is not authorized for the operation
    INDETERMINATE_IDENTITY,          // Operation not allowed due to identity being indeterminate-
    EXTERNAL_SYSTEM_PROBLEM,         // Other system in unavailable, gave technical error response
    MODULE_PROBLEM,                  // Problem that occured inside a module
    UNKNOWN_INTERNAL_PROBLEM,        // All others
    DATA_NOT_FOUND,                  // Certificate (or other resource) not found
    CERTIFICATE_REVOKED,
    CONCURRENT_MODIFICATION,
    GRP_PROBLEM,
    MISSING_PARAMETER

    // @formatter:on
}
