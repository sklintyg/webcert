package se.inera.intyg.webcert.fkstub.validation;

import java.util.List;

import com.google.common.base.Joiner;

/**
 * @author andreaskaltenbach
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String VALIDATION_ERROR_PREFIX = "Validation Error(s) found: ";

    public ValidationException(String message) {
        super(VALIDATION_ERROR_PREFIX + message);
    }

    public ValidationException(List<String> messages) {
        super(VALIDATION_ERROR_PREFIX + Joiner.on("\n").join(messages));
    }
}
