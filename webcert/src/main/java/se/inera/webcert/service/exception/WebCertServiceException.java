package se.inera.webcert.service.exception;

/**
 * Runtime exception used to signal internal errors.
 * 
 * @author nikpet
 *
 */
public class WebCertServiceException extends RuntimeException {

    private WebCertServiceErrorCodeEnum errorCode;

    public WebCertServiceException() {
    }

    /**
     * Constructor with just errorCode
     * @param errorCode
     */
    public WebCertServiceException(WebCertServiceErrorCodeEnum errorCode) {
        super();
        this.errorCode = errorCode;
    }

    /**
     * @param errorCode
     * @param message - Custom error message
     */
    public WebCertServiceException(WebCertServiceErrorCodeEnum errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructor with original exception
     * @param errorCode
     * @param cause
     */
    public WebCertServiceException(WebCertServiceErrorCodeEnum errorCode, Exception cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public WebCertServiceErrorCodeEnum getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(WebCertServiceErrorCodeEnum errorCode) {
        this.errorCode = errorCode;
    }

}
