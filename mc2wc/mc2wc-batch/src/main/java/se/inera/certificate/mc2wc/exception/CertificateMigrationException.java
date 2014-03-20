package se.inera.certificate.mc2wc.exception;

public class CertificateMigrationException extends AbstractCertificateMigrationException {

    private static final long serialVersionUID = -4057720226321448326L;

    public CertificateMigrationException() {
        super();
    }

    public CertificateMigrationException(String message) {
        super(message);
    }

    public CertificateMigrationException(Throwable cause) {
        super(cause);
    }

    public CertificateMigrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CertificateMigrationException(String message, Throwable cause, boolean enableSuppression,
                                         boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
