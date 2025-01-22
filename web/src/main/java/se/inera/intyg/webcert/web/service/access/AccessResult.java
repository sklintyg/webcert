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

import javax.validation.constraints.NotNull;

/**
 * Class that wraps the result of an access service evaluation. Contains AccessResultCode and String message
 * from the access evaluation including convenience methods.
 */
public final class AccessResult {

    private AccessResultCode code;
    private String message;

    /**
     * Create access result that allows access. Code will be NO_PROBLEM and message empty String.
     *
     * @return Created access result
     */
    public static AccessResult noProblem() {
        return new AccessResult(AccessResultCode.NO_PROBLEM, "");
    }

    /**
     * Create access result. Use this method when creating access result that denies access. If access is
     * allowed, then use noProblem().
     *
     * @param code Code as the result of the evaluation.
     * @param message Message explaining the reason for access denied.
     * @return Created access result
     */
    public static AccessResult create(@NotNull AccessResultCode code, @NotNull String message) {
        return new AccessResult(code, message);
    }

    private AccessResult(AccessResultCode code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Code containing the result of the access evaluation.
     *
     * @return Code is NO_PROBLEM if access allowed. Any other code means access is denied.
     */
    @NotNull
    public AccessResultCode getCode() {
        return code;
    }

    /**
     * Message why access is denied.
     *
     * @return Message is empty if access is allowed.
     */
    @NotNull
    public String getMessage() {
        return message;
    }

    /**
     * If access was given.
     *
     * @return true if user has access.
     */
    public boolean isAllowed() {
        return code.equals(AccessResultCode.NO_PROBLEM);
    }

    /**
     * If access was denied.
     *
     * @return true if user is denied access.
     */
    public boolean isDenied() {
        return !code.equals(AccessResultCode.NO_PROBLEM);
    }
}
