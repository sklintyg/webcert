package se.inera.intyg.webcert.logsender.exception;

/**
 * Created by eriklupander on 2016-02-29.
 */
public class PermanentException extends Exception {
    public PermanentException(String resultText) {
        super(resultText);
    }
}
