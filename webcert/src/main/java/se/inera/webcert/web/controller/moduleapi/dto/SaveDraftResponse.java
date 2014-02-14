package se.inera.webcert.web.controller.moduleapi.dto;

import java.util.ArrayList;
import java.util.List;

public class SaveDraftResponse {

    private DraftValidationStatus status;

    private List<SaveDraftValidationMessage> messages = new ArrayList<SaveDraftValidationMessage>();

    public SaveDraftResponse() {

    }

    public SaveDraftResponse(DraftValidationStatus status) {
        this.status = status;
    }

    public DraftValidationStatus getStatus() {
        return status;
    }

    public void setStatus(DraftValidationStatus status) {
        this.status = status;
    }

    public List<SaveDraftValidationMessage> getMessages() {
        return messages;
    }

    public void getMessage(List<SaveDraftValidationMessage> messages) {
        this.messages = messages;
    }

    public void addMessage(String field, String message) {
        messages.add(new SaveDraftValidationMessage(field, message));
    }
}
