package se.inera.webcert.modules.api.dto;

import java.util.ArrayList;
import java.util.List;

public class DraftValidationResponse {

    private DraftValidationStatus status;

    private List<DraftValidationMessage> validationErrors = new ArrayList<DraftValidationMessage>();

    public DraftValidationResponse() {

    }

    public boolean checkIfValidAndEmpty() {
        return (DraftValidationStatus.VALID.equals(this.status)) && (this.validationErrors.isEmpty());
    }

    public DraftValidationStatus getStatus() {
        return status;
    }

    public void setStatus(DraftValidationStatus status) {
        this.status = status;
    }

    public List<DraftValidationMessage> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<DraftValidationMessage> validationErrors) {
        this.validationErrors = validationErrors;
    }

}
