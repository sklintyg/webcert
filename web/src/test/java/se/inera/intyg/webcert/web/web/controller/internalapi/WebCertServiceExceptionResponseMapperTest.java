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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;

class WebCertServiceExceptionResponseMapperTest {

  private final WebCertServiceExceptionResponseMapper mapper =
      new WebCertServiceExceptionResponseMapper();

  @Test
  void shallReturnNotFoundWhenDataNotFound() {
    final var exception =
        new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "message");

    final var response = mapper.toResponseEntity(exception);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void shallReturnBadRequestWhenMissingParameter() {
    final var exception =
        new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER, "message");

    final var response = mapper.toResponseEntity(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void shallReturnInternalServerErrorWhenUnknownInternalProblem() {
    final var exception =
        new WebCertServiceException(
            WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, "message");

    final var response = mapper.toResponseEntity(exception);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void shallReturnInternalServerErrorForAnyOtherErrorCode() {
    final var exception =
        new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "message");

    final var response = mapper.toResponseEntity(exception);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}
