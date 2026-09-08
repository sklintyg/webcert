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

  /**
   * True if webcert responded with a 4xx (i.e. our request was invalid in some way).
   */
  public boolean isClientError() {
    return statusCode.is4xxClientError();
  }

  /**
   * True if webcert responded with a 5xx (i.e. webcert itself failed).
   */
  public boolean isServerError() {
    return statusCode.is5xxServerError();
  }
}
