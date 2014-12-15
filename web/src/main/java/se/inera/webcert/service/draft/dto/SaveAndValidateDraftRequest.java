package se.inera.webcert.service.draft.dto;

import se.inera.webcert.service.dto.HoSPerson;

public class SaveAndValidateDraftRequest {

    private String intygId;

    private String draftAsJson;

    private Boolean autoSave;

    private HoSPerson savedBy;

    public SaveAndValidateDraftRequest() {

    }

    public String getIntygId() {
        return intygId;
    }

    public void setIntygId(String intygId) {
        this.intygId = intygId;
    }

    public String getDraftAsJson() {
        return draftAsJson;
    }

    public void setDraftAsJson(String draftAsJson) {
        this.draftAsJson = draftAsJson;
    }

    public Boolean getAutoSave() { return autoSave; }

    public void setAutoSave(Boolean autoSave) { this.autoSave = autoSave; }

    public HoSPerson getSavedBy() {
        return savedBy;
    }

    public void setSavedBy(HoSPerson savedBy) {
        this.savedBy = savedBy;
    }

}
