package se.inera.certificate.mc2wc.exception;

public abstract class AbstractCertificateMigrationException extends RuntimeException {

    private static final long serialVersionUID = -1960891557306449738L;

    public AbstractCertificateMigrationException() {
        super();
    }

    public AbstractCertificateMigrationException(String message) {
        super(message);
    }

    public AbstractCertificateMigrationException(Throwable cause) {
        super(cause);
    }

    public AbstractCertificateMigrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AbstractCertificateMigrationException(String message, Throwable cause, boolean enableSuppression,
                                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
