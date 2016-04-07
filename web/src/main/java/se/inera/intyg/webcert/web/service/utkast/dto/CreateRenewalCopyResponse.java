package se.inera.intyg.webcert.web.service.utkast.dto;

public class CreateRenewalCopyResponse {
    private String newDraftIntygType;

    private String newDraftIntygId;

    private String originalIntygId;

    public CreateRenewalCopyResponse(String newDraftIntygType, String newDraftIntygId, String originalIntygId) {
        this.newDraftIntygId = newDraftIntygId;
        this.newDraftIntygType = newDraftIntygType;
        this.originalIntygId = originalIntygId;
    }

    public String getNewDraftIntygType() {
        return newDraftIntygType;
    }

    public String getNewDraftIntygId() {
        return newDraftIntygId;
    }

    public String getOriginalIntygId() {
        return originalIntygId;
    }
}
