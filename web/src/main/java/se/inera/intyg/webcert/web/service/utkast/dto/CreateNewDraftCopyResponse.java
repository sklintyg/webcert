package se.inera.intyg.webcert.web.service.utkast.dto;

public class CreateNewDraftCopyResponse {

    private String newDraftIntygType;

    private String newDraftIntygId;

    public CreateNewDraftCopyResponse(String newDraftIntygType, String newDraftIntygId) {
        this.newDraftIntygId = newDraftIntygId;
        this.newDraftIntygType = newDraftIntygType;
    }

    public String getNewDraftIntygType() {
        return newDraftIntygType;
    }

    public String getNewDraftIntygId() {
        return newDraftIntygId;
    }
}
