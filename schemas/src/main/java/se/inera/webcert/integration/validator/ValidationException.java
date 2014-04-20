package se.inera.webcert.integration.validator;

import java.util.List;

import com.google.common.base.Joiner;

/**
 * @author andreaskaltenbach
 */
public class ValidationException extends RuntimeException {
    private static final String VALIDATION_ERROR_PREFIX = "Validation Error(s) found: ";

    public ValidationException(String message) {
        super(VALIDATION_ERROR_PREFIX + message);
    }

    public ValidationException(List<String> messages) {
        super(VALIDATION_ERROR_PREFIX + Joiner.on("\n").join(messages));
    }
}
