package se.inera.webcert.notifications.service.exception;

public class NonRecoverableCertificateStatusUpdateServiceException extends Exception {
    private static final long serialVersionUID = -8184379033317261358L;

    public NonRecoverableCertificateStatusUpdateServiceException() {
    }

    public NonRecoverableCertificateStatusUpdateServiceException(String message) {
        super(message);
    }

    public NonRecoverableCertificateStatusUpdateServiceException(Throwable cause) {
        super(cause);
    }
    
    public NonRecoverableCertificateStatusUpdateServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
