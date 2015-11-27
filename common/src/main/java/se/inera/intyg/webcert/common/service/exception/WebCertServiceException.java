package se.inera.intyg.webcert.common.service.exception;

/**
 * Runtime exception used to signal internal errors.
 *
 * @author nikpet
 */
public final class WebCertServiceException extends RuntimeException {

    private static final long serialVersionUID = -5060049906425434207L;

    private final WebCertServiceErrorCodeEnum errorCode;

    /**
     * @param errorCode error code
     * @param message   - Custom error message
     */
    public WebCertServiceException(WebCertServiceErrorCodeEnum errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructor with original exception.
     *
     * @param errorCode error code
     * @param cause cause
     */
    public WebCertServiceException(WebCertServiceErrorCodeEnum errorCode, Exception cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    /**
     * Constructor with message and original exception.
     *
     * @param errorCode error code
     * @param message message
     * @param cause cause
     */
    public WebCertServiceException(WebCertServiceErrorCodeEnum errorCode, String message, Exception cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public WebCertServiceErrorCodeEnum getErrorCode() {
        return errorCode;
    }

}
