package se.inera.intyg.webcert.notification_sender.exception;

public class TemporaryException extends Exception {
    private static final long serialVersionUID = -8184379033317261358L;

    public TemporaryException() {
    }

    public TemporaryException(String message) {
        super(message);
    }

    public TemporaryException(Throwable cause) {
        super(cause);
    }

}
