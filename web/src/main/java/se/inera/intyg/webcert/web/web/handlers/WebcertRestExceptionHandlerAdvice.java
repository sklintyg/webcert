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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.infra.security.authorities.AuthoritiesException;

@RestControllerAdvice(
    basePackages = {
      "se.inera.intyg.webcert.web.web.controller.api",
      "se.inera.intyg.webcert.web.web.controller.facade",
      "se.inera.intyg.webcert.web.web.controller.moduleapi",
      "se.inera.intyg.webcert.web.web.controller.internalapi",
      "se.inera.intyg.webcert.web.web.controller.authtestability",
      "se.inera.intyg.webcert.web.web.controller.testability",
      "se.inera.intyg.webcert.web.web.controller.testability.facade"
    })
public class WebcertRestExceptionHandlerAdvice {

  private static final Logger LOG =
      LoggerFactory.getLogger(WebcertRestExceptionHandlerAdvice.class);

  @ExceptionHandler(WebCertServiceException.class)
  public ResponseEntity<WebcertRestExceptionResponse> handleWebCertServiceException(
      WebCertServiceException e) {
    if (e.getErrorCode() != WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION) {
      LOG.warn(
          "Internal exception occured! Internal error code: {} Error message: {}",
          e.getErrorCode(),
          e.getMessage(),
          e);
    }
    return ResponseEntity.internalServerError()
        .body(new WebcertRestExceptionResponse(e.getErrorCode(), e.getMessage()));
  }

  @ExceptionHandler(AuthoritiesException.class)
  public ResponseEntity<WebcertRestExceptionResponse> handleAuthoritiesException(
      AuthoritiesException e) {
    LOG.warn("AuthValidation occured: ", e);
    return ResponseEntity.internalServerError()
        .body(
            new WebcertRestExceptionResponse(
                WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, e.getMessage()));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<WebcertRestExceptionResponse> handleRuntimeException(RuntimeException e) {
    LOG.error("Unhandled RuntimeException occured!", e);
    return ResponseEntity.internalServerError()
        .body(
            new WebcertRestExceptionResponse(
                WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, e.getMessage()));
  }
}
