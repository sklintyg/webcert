package se.inera.certificate.mc2wc.service;

public class MigrationServiceException extends Exception {

    private static final long serialVersionUID = -1828894780131973648L;

    public MigrationServiceException() {

    }

    public MigrationServiceException(String message) {
        super(message);
    }

    public MigrationServiceException(Throwable cause) {
        super(cause);
    }

    public MigrationServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
