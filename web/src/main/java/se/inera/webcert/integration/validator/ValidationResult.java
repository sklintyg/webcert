package se.inera.webcert.integration.validator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class ValidationResult {
    
    private List<String> errors = new ArrayList<String>();
    
    public static ValidationResult newInstance() {
        return new ValidationResult();
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
