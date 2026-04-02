/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;

/**
 * Spring MVC replacement for {@link WebcertRedirectIntegrationExceptionHandler}. Handles exceptions
 * thrown from integration redirect controllers by issuing HTTP 303 redirects to the error page.
 */
@ControllerAdvice(
    basePackages = {
      "se.inera.intyg.webcert.web.web.controller.integration",
      "se.inera.intyg.webcert.web.web.controller.legacyintegration"
    })
public class WebcertRedirectExceptionHandlerAdvice {

  private static final Logger LOG =
      LoggerFactory.getLogger(WebcertRedirectExceptionHandlerAdvice.class);

  public static final String ERROR_REASON_MISSING_PARAMETER = "missing-parameter";
  public static final String ERROR_REASON_AUTH_EXCEPTION = "auth-exception";
  public static final String ERROR_REASON_AUTH_EXCEPTION_SUBSRIPTION =
      "auth-exception-subscription";
  public static final String ERROR_REASON_AUTH_EXCEPTION_SEKRETESSMARKERING =
      "auth-exception-sekretessmarkering";
  public static final String ERROR_REASON_AUTH_EXCEPTION_USER_ALREADY_ACTIVE =
      "auth-exception-user-already-active";
  public static final String ERROR_REASON_PU_PROBLEM = "pu-problem";
  public static final String ERROR_REASON_UNKNOWN = "unknown";

  @Autowired private ReactUriFactory reactUriFactory;

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Void> handleException(RuntimeException ex, HttpServletRequest request) {
    if (ex instanceof AuthoritiesException) {
      return handleAuthorityException(ex, request);
    }
    return handleRuntimeException(ex, request);
  }

  private ResponseEntity<Void> handleAuthorityException(Exception ex, HttpServletRequest request) {
    LOG.warn("AuthValidation exception occured: ", ex);
    if (ex instanceof MissingSubscriptionException) {
      return redirectResponse(ERROR_REASON_AUTH_EXCEPTION_SUBSRIPTION, request);
    }
    return redirectResponse(ERROR_REASON_AUTH_EXCEPTION, request);
  }

  private ResponseEntity<Void> handleRuntimeException(
      RuntimeException ex, HttpServletRequest request) {
    if (ex instanceof WebCertServiceException serviceEx) {
      LOG.warn("WebCertServiceException caught: {}", ex.getMessage(), ex);
      return switch (serviceEx.getErrorCode()) {
        case MISSING_PARAMETER -> redirectResponse(ERROR_REASON_MISSING_PARAMETER, request);
        case AUTHORIZATION_PROBLEM -> redirectResponse(ERROR_REASON_AUTH_EXCEPTION, request);
        case AUTHORIZATION_PROBLEM_SEKRETESSMARKERING ->
            redirectResponse(ERROR_REASON_AUTH_EXCEPTION_SEKRETESSMARKERING, request);
        case AUTHORIZATION_USER_SESSION_ALREADY_ACTIVE ->
            redirectResponse(ERROR_REASON_AUTH_EXCEPTION_USER_ALREADY_ACTIVE, request);
        case PU_PROBLEM -> redirectResponse(ERROR_REASON_PU_PROBLEM, request);
        default -> redirectResponse(ERROR_REASON_UNKNOWN, request);
      };
    }
    LOG.error("Unhandled RuntimeException occured!", ex);
    return redirectResponse(ERROR_REASON_UNKNOWN, request);
  }

  private ResponseEntity<Void> redirectResponse(String errorCode, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.SEE_OTHER)
        .location(reactUriFactory.uriForErrorResponse(request, errorCode))
        .build();
  }
}
