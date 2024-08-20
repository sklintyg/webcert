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

import java.net.URI;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;


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

    @Context
    UriInfo uriInfo;

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
            return buildErrorRedirectResponse(ERROR_REASON_AUTH_EXCEPTION_SUBSRIPTION, e.getMessage());
        }
        return buildErrorRedirectResponse(ERROR_REASON_AUTH_EXCEPTION, e.getMessage());
    }

    private Response handleRuntimeException(RuntimeException re) {
        if (re instanceof WebCertServiceException) {
            LOG.warn("WebCertServiceException caught", re.getMessage());
            WebCertServiceException we = (WebCertServiceException) re;
            if (we.getErrorCode() == WebCertServiceErrorCodeEnum.MISSING_PARAMETER) {
                return buildErrorRedirectResponse(ERROR_REASON_MISSING_PARAMETER, we.getMessage());
            } else if (we.getErrorCode() == WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM) {
                return buildErrorRedirectResponse(ERROR_REASON_AUTH_EXCEPTION, we.getMessage());
            } else if (we.getErrorCode() == WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING) {
                return buildErrorRedirectResponse(ERROR_REASON_AUTH_EXCEPTION_SEKRETESSMARKERING, we.getMessage());
            } else if (we.getErrorCode() == WebCertServiceErrorCodeEnum.AUTHORIZATION_USER_SESSION_ALREADY_ACTIVE) {
                return buildErrorRedirectResponse(ERROR_REASON_AUTH_EXCEPTION_USER_ALREADY_ACTIVE, we.getMessage());
            } else if (we.getErrorCode() == WebCertServiceErrorCodeEnum.PU_PROBLEM) {
                return buildErrorRedirectResponse(ERROR_REASON_PU_PROBLEM, we.getMessage());
            }
        }
        LOG.error("Unhandled RuntimeException occured!", re);
        return buildErrorRedirectResponse("unknown", re.getMessage());
    }

    private Response buildErrorRedirectResponse(String errorReason, String message) {
        URI location = ERROR_REASON_MISSING_PARAMETER.equals(errorReason)
            ? uriInfo.getBaseUriBuilder().replacePath("/error")
            .queryParam("reason", errorReason)
            .queryParam("message", message)
            .build()
            : uriInfo.getBaseUriBuilder().replacePath("/error")
                .queryParam("reason", errorReason)
                .build();

        return Response.temporaryRedirect(location).build();
    }
}
