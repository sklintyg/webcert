package se.inera.webcert.modules.api.dto;

public class DraftValidationMessage {

    private String field;
    
    private String message;
    
    public DraftValidationMessage() {
        
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
