/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.fkstub.validation;

import java.util.List;

import com.google.common.base.Joiner;

/**
 * @author andreaskaltenbach
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String VALIDATION_ERROR_PREFIX = "Validation Error(s) found: ";

    public ValidationException(String message) {
        super(VALIDATION_ERROR_PREFIX + message);
    }

    public ValidationException(List<String> messages) {
        super(VALIDATION_ERROR_PREFIX + Joiner.on("\n").join(messages));
    }
}
