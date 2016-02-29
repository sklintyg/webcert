package se.inera.intyg.webcert.logsender.exception;

/**
 * Created by eriklupander on 2016-02-29.
 */
public class TemporaryException extends Exception {
    public TemporaryException(String resultText) {
        super(resultText);
    }
}
