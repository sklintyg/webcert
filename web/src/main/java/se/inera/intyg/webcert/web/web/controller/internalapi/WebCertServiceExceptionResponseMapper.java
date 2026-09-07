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
package se.inera.intyg.webcert.web.web.controller.internalapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;

/**
 * Maps a {@link WebCertServiceException} to an appropriate {@link ResponseEntity}, based on the
 * exception's {@link WebCertServiceErrorCodeEnum}.
 */
@Component
@Slf4j
public class WebCertServiceExceptionResponseMapper {

  public <T> ResponseEntity<T> toResponseEntity(RuntimeException exception) {
    if (exception instanceof ResourceAccessException resourceAccessException) {
      log.error(
          "ResourceAccessException occurred while calling external service: {}",
          resourceAccessException.getMessage(),
          resourceAccessException);
      return ResponseEntity.internalServerError().build();
    } else if (exception instanceof WebCertServiceException webCertServiceException) {
      final var errorCode = webCertServiceException.getErrorCode();
      if (errorCode == WebCertServiceErrorCodeEnum.DATA_NOT_FOUND) {
        log.info("Data not found for request: {}", webCertServiceException.getMessage());
        return ResponseEntity.notFound().build();
      } else if (errorCode == WebCertServiceErrorCodeEnum.MISSING_PARAMETER) {
        log.info("Missing parameter for request: {}", webCertServiceException.getMessage());
        return ResponseEntity.badRequest().build();
      } else {
        log.warn(
            "Unhandled WebCertServiceException occurred: {}",
            webCertServiceException.getMessage(),
            webCertServiceException);
        return ResponseEntity.internalServerError().build();
      }
    } else {
      log.error("Unhandled RuntimeException occurred: {}", exception.getMessage(), exception);
      return ResponseEntity.internalServerError().build();
    }
  }
}
