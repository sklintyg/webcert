package se.inera.webcert.service.utkast.dto;


public class SaveAndValidateDraftResponse {

    private long version;

    private DraftValidation draftValidation;

    public SaveAndValidateDraftResponse(long version, DraftValidation draftValidation) {
        this.version = version;
        this.draftValidation = draftValidation;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public DraftValidation getDraftValidation() {
        return draftValidation;
    }

    public void setDraftValidation(DraftValidation draftValidation) {
        this.draftValidation = draftValidation;
    }


}
