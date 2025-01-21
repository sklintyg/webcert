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
package se.inera.intyg.webcert.web.web.util.access;

import se.inera.intyg.webcert.web.service.access.AccessResult;

/**
 * Helper to throw exceptions based on AccessResult.
 */
public interface AccessResultExceptionHelper {

    /**
     * Throw an exception of correct type based on the ActionResult.
     *
     * @param actionResult ActionResult to consider when throwing the exception.
     */
    void throwException(AccessResult actionResult);

    /**
     * Throw an exception of correct type based on the ActionResults. An exception is only thrown
     * if the ActionResultType is NOT NO_PROBLEM.
     *
     * @param actionResult ActionResult to evaluate and base exception on.
     */
    void throwExceptionIfDenied(AccessResult actionResult);
}
