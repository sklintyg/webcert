package se.inera.certificate.mc2wc.exception;

public class FatalCertificateMigrationException extends AbstractCertificateMigrationException {

    private static final long serialVersionUID = -2520770963622800573L;

    public FatalCertificateMigrationException() {
        super();
    }

    public FatalCertificateMigrationException(String message) {
        super(message);
    }

    public FatalCertificateMigrationException(Throwable cause) {
        super(cause);
    }

    public FatalCertificateMigrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FatalCertificateMigrationException(String message, Throwable cause, boolean enableSuppression,
                                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
