/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

/**
 * Runtime exception used to signal internal errors.
 *
 * @author nikpet
 */
public final class WebCertServiceException extends RuntimeException {

    private static final long serialVersionUID = -5060049906425434207L;

    private final WebCertServiceErrorCodeEnum errorCode;

    /**
     * @param errorCode error code
     * @param message   - Custom error message
     */
    public WebCertServiceException(WebCertServiceErrorCodeEnum errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructor with original exception.
     *
     * @param errorCode error code
     * @param cause cause
     */
    public WebCertServiceException(WebCertServiceErrorCodeEnum errorCode, Exception cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    /**
     * Constructor with message and original exception.
     *
     * @param errorCode error code
     * @param message message
     * @param cause cause
     */
    public WebCertServiceException(WebCertServiceErrorCodeEnum errorCode, String message, Exception cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public WebCertServiceErrorCodeEnum getErrorCode() {
        return errorCode;
    }

}
