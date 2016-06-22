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

package se.inera.intyg.webcert.web.web.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.common.security.authorities.AuthoritiesException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import java.net.URI;


/**
 * Exception handler for integration redirect handlers. It issues a redirect to the error page (with a reason parameter)
 * so that the user will get an actual page when errors occurs in the integration-link-redirect proccess.
 *
 * @author marced
 */
public class WebcertRedirectIntegrationExceptionHandler implements ExceptionMapper<RuntimeException> {

    private static final Logger LOG = LoggerFactory.getLogger(WebcertRedirectIntegrationExceptionHandler.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(RuntimeException e) {

        if (e instanceof AuthoritiesException) {
            return handleAuthorityException((AuthoritiesException) e);
        } else {
            return handleRuntimeException(e);
        }

    }

    /**
     * The user requested an operation that caused an Auth check to fail.
     *
     * @param e
     * @return
     */
    private Response handleAuthorityException(AuthoritiesException e) {
        LOG.warn("AuthValidation occured: ", e);
        return buildErrorRedirectResponse("auth-exception", e.getMessage());
    }

    private Response handleRuntimeException(RuntimeException re) {
        LOG.error("Unhandled RuntimeException occured!", re);
        if (re instanceof WebCertServiceException) {
            WebCertServiceException we = (WebCertServiceException) re;
            if (we.getErrorCode() == WebCertServiceErrorCodeEnum.MISSING_PARAMETER) {
                return buildErrorRedirectResponse("missing-parameter", we.getMessage());
            }
        }
        return buildErrorRedirectResponse("unknown", re.getMessage());
    }

    private Response buildErrorRedirectResponse(String errorReason, String message) {
        URI location = uriInfo.getBaseUriBuilder().replacePath("/error.jsp").
                queryParam("reason", errorReason).
                queryParam("message", message).
                build();
        return Response.temporaryRedirect(location).build();
    }
}
