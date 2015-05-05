package se.inera.webcert.service.utkast.dto;

import java.util.ArrayList;
import java.util.List;

public class DraftValidation {

    private DraftValidationStatus status = DraftValidationStatus.VALID;

    private List<DraftValidationMessage> messages = new ArrayList<DraftValidationMessage>();

    public DraftValidation() {
    }

    public DraftValidationStatus getStatus() {
        return status;
    }

    public void setStatus(DraftValidationStatus status) {
        this.status = status;
    }

    public void addMessage(DraftValidationMessage message) {
        this.messages.add(message);
    }

    public List<DraftValidationMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<DraftValidationMessage> messages) {
        this.messages = messages;
    }

    public boolean isDraftValid() {
        return (DraftValidationStatus.VALID.equals(this.status));
    }
}
