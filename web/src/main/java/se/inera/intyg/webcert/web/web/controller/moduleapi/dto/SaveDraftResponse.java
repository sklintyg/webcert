package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

import se.inera.certificate.modules.support.api.dto.ValidationMessageType;

import java.util.ArrayList;
import java.util.List;

public class SaveDraftResponse {

    private long version;

    private DraftValidationStatus status;

    private List<SaveDraftValidationMessage> messages = new ArrayList<>();

    public SaveDraftResponse(long version, DraftValidationStatus status) {
        this.version = version;
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

    public void addMessage(String field, ValidationMessageType type, String message) {
        messages.add(new SaveDraftValidationMessage(field, type, message));
    }

    public long getVersion() {
        return version;
    }
}
