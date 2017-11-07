package se.inera.intyg.webcert.integration.tak.model;

import java.util.List;

public class TakResult {
    private final boolean valid;
    private final List<String> errorMessages;

    public TakResult(boolean valid, List<String> errorMessages) {
        this.valid = valid;
        this.errorMessages = errorMessages;
    }

    public boolean isValid() {
        return this.valid;
    }

    public List<String> getErrorMessages() {
        return this.errorMessages;
    }
}
