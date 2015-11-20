package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

import se.inera.certificate.modules.support.api.dto.ValidationMessageType;

public class SaveDraftValidationMessage {

    private String field;

    private ValidationMessageType type;

    private String message;

    public SaveDraftValidationMessage() {

    }

    public SaveDraftValidationMessage(String field, ValidationMessageType type, String message) {
        super();
        this.field = field;
        this.type = type;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public ValidationMessageType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
