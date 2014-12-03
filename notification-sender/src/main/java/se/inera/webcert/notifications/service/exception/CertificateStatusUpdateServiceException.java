package se.inera.webcert.notifications.service.exception;

public class CertificateStatusUpdateServiceException extends Exception {
    private static final long serialVersionUID = -8184379033317261358L;

    public CertificateStatusUpdateServiceException() {
    }

    public CertificateStatusUpdateServiceException(String message) {
        super(message);
    }

    public CertificateStatusUpdateServiceException(Throwable cause) {
        super(cause);
    }

}
