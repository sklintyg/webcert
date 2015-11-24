package se.inera.intyg.webcert.notification_sender.exception;

public class PermanentException extends Exception {
    private static final long serialVersionUID = -8184379033317261358L;

    public PermanentException() {
    }

    public PermanentException(String message) {
        super(message);
    }

    public PermanentException(Throwable cause) {
        super(cause);
    }

    public PermanentException(String message, Throwable cause) {
        super(message, cause);
    }

}
