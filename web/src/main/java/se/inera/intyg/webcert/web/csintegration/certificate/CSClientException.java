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
package se.inera.intyg.webcert.web.csintegration.certificate;

import org.springframework.http.HttpStatusCode;

public class CSClientException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final HttpStatusCode statusCode;

  public CSClientException(String message, HttpStatusCode statusCode) {
    super(message);
    this.statusCode = statusCode;
  }

  public HttpStatusCode getStatusCode() {
    return statusCode;
  }

  /** True if webcert responded with a 4xx (i.e. our request was invalid in some way). */
  public boolean isClientError() {
    return statusCode.is4xxClientError();
  }

  /** True if webcert responded with a 5xx (i.e. webcert itself failed). */
  public boolean isServerError() {
    return statusCode.is5xxServerError();
  }
}
