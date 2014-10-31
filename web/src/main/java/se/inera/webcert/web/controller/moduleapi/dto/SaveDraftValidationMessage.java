package se.inera.webcert.web.controller.moduleapi.dto;

public class SaveDraftValidationMessage {

    private String field;

    private String message;

    public SaveDraftValidationMessage() {

    }

    public SaveDraftValidationMessage(String field, String message) {
        super();
        this.field = field;
        this.message = message;
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
