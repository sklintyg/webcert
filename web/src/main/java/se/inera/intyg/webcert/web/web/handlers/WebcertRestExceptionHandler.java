/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.handlers;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;

/**
 * Exception handler for REST services. Runtime exceptions thrown as {@link WebCertServiceException}
 * WebCertServiceException are logged as warnings. All other runtime exceptions thrown are treated as errors and
 * logged as such.
 *
 * @author nikpet
 */
public class WebcertRestExceptionHandler implements ExceptionMapper<RuntimeException> {

    private static final Logger LOG = LoggerFactory.getLogger(WebcertRestExceptionHandler.class);

    @Override
    public Response toResponse(RuntimeException e) {

        if (e instanceof WebCertServiceException) {
            // If this is an exception thrown by our code, we have a more specific error code
            return handleWebCertServiceException((WebCertServiceException) e);
        } else if (e instanceof AuthoritiesException) {
            return handleAuthorityException((AuthoritiesException) e);
        }

        return handleRuntimeException(e);

    }

    private Response handleAuthorityException(AuthoritiesException e) {
        LOG.warn("AuthValidation occured: ", e);
        WebcertRestExceptionResponse exceptionResponse = new WebcertRestExceptionResponse(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
            e.getMessage());
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exceptionResponse).type(MediaType.APPLICATION_JSON)
            .build();
    }

    /**
     * Exceptions thrown by us should be logged as warnings as they are thrown deliberately.
     *
     * @param wcse A WebCertServiceException
     */
    private Response handleWebCertServiceException(WebCertServiceException wcse) {
        // Don't log concurrent modifiation exceptions, they are logged elsewhere
        if (wcse.getErrorCode() != WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION) {
            LOG.warn("Internal exception occured! Internal error code: {} Error message: {}", wcse.getErrorCode(),
                wcse.getMessage());
        }
        WebcertRestExceptionResponse exceptionResponse = new WebcertRestExceptionResponse(wcse.getErrorCode(), wcse.getMessage());
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exceptionResponse).type(MediaType.APPLICATION_JSON)
            .build();
    }

    /**
     * All other runtime exceptions are logged as errors as they are not thrown deliberately.
     *
     * @param re A RuntimeException
     */
    private Response handleRuntimeException(RuntimeException re) {
        LOG.error("Unhandled RuntimeException occured!", re);
        WebcertRestExceptionResponse exceptionResponse = new WebcertRestExceptionResponse(
            WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM,
            re.getMessage());
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exceptionResponse).type(MediaType.APPLICATION_JSON)
            .build();
    }
}
