package se.inera.intyg.webcert.web.integration.validator;

/**
 * Created by eriklupander on 2017-09-11.
 */
public class ErrorType {

    private String message;
    private String type;

    public ErrorType(String message) {
        this.message = message;
        this.type = "VALIDATION_ERROR";
    }

    public ErrorType(String message, String type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }
}
