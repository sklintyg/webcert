package se.inera.intyg.webcert.web.integration.validator;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultValidator {

    private final List<String> errors = new ArrayList<>();

    public static ResultValidator newInstance() {
        return new ResultValidator();
    }

    public void addError(String msg) {
        errors.add(msg);
    }

    public void addError(String msgTemplate, String... args) {
        String msg = MessageFormat.format(msgTemplate, (Object[]) args);
        addError(msg);
    }

    public void addErrors(List<String> msgs) {
        errors.addAll(msgs);
    }

    public List<String> getErrorMessages() {
        return errors;
    }

    public String getErrorMessagesAsString() {
        return StringUtils.join(errors, ", ");
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void reset() {
        errors.clear();
    }
}
