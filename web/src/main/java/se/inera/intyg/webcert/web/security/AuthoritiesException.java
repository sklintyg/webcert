/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.security;

import org.springframework.security.core.AuthenticationException;

/**
 * Created by Magnus Ekstrand on 16/09/15.
 */
public class AuthoritiesException extends AuthenticationException {

    private static final long serialVersionUID = -4951218504958281758L;

    /**
     * Constructs an {@code AuthenticationException} with the specified message and root cause.
     *
     * @param msg the detail message
     * @param t   the root cause
     */
    public AuthoritiesException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * Constructs an {@code AuthenticationException} with the specified message and no root cause.
     *
     * @param msg the detail message
     */
    public AuthoritiesException(String msg) {
        super(msg);
    }

}
