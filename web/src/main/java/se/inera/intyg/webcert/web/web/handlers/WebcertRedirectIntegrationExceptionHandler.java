/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;


/**
 * Exception handler for integration redirect handlers. It issues a redirect to the error page (with a reason parameter)
 * so that the user will get an actual page when errors occurs in the integration-link-redirect proccess.
 *
 * @author marced
 */
public class WebcertRedirectIntegrationExceptionHandler implements ExceptionMapper<RuntimeException> {

    private static final Logger LOG = LoggerFactory.getLogger(WebcertRedirectIntegrationExceptionHandler.class);

    public static final String ERROR_REASON_MISSING_PARAMETER = "missing-parameter";
    public static final String ERROR_REASON_AUTH_EXCEPTION = "auth-exception";
    public static final String ERROR_REASON_AUTH_EXCEPTION_SUBSRIPTION = "auth-exception-subscription";
    public static final String ERROR_REASON_AUTH_EXCEPTION_SEKRETESSMARKERING = "auth-exception-sekretessmarkering";
    public static final String ERROR_REASON_AUTH_EXCEPTION_USER_ALREADY_ACTIVE = "auth-exception-user-already-active";
    public static final String ERROR_REASON_PU_PROBLEM = "pu-problem";
    public static final String ERROR_REASON_UNKNOWN = "unknown";

    @Context
    UriInfo uriInfo;

    @Autowired
    private ReactUriFactory reactUriFactory;

    @Override
    public Response toResponse(RuntimeException e) {

        if (e instanceof AuthoritiesException) {
            return handleAuthorityException(e);
        } else {
            return handleRuntimeException(e);
        }
    }

    /**
     * The user requested an operation that caused an Auth check to fail.
     */
    private Response handleAuthorityException(Exception e) {
        LOG.warn("AuthValidation exception occured: ", e);
        if (e instanceof MissingSubscriptionException) {
            return getRedirectResponse(ERROR_REASON_AUTH_EXCEPTION_SUBSRIPTION);
        }
        return getRedirectResponse(ERROR_REASON_AUTH_EXCEPTION);
    }

    private Response handleRuntimeException(RuntimeException e) {
        if (e instanceof WebCertServiceException) {
            LOG.warn("WebCertServiceException caught: {}", e.getMessage(), e);
            return switch (((WebCertServiceException) e).getErrorCode()) {
                case MISSING_PARAMETER -> getRedirectResponse(ERROR_REASON_MISSING_PARAMETER);
                case AUTHORIZATION_PROBLEM -> getRedirectResponse(ERROR_REASON_AUTH_EXCEPTION);
                case AUTHORIZATION_PROBLEM_SEKRETESSMARKERING -> getRedirectResponse(ERROR_REASON_AUTH_EXCEPTION_SEKRETESSMARKERING);
                case AUTHORIZATION_USER_SESSION_ALREADY_ACTIVE -> getRedirectResponse(ERROR_REASON_AUTH_EXCEPTION_USER_ALREADY_ACTIVE);
                case PU_PROBLEM -> getRedirectResponse(ERROR_REASON_PU_PROBLEM);
                default -> getRedirectResponse(ERROR_REASON_UNKNOWN);
            };
        }

        LOG.error("Unhandled RuntimeException occured!", e);
        return getRedirectResponse(ERROR_REASON_UNKNOWN);
    }

    private Response getRedirectResponse(String errorCode) {
        return Response.seeOther(reactUriFactory.uriForErrorResponse(uriInfo, errorCode)).build();
    }

}
