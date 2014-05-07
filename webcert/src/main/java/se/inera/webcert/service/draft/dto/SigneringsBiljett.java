package se.inera.webcert.service.draft.dto;

public class SigneringsBiljett {

    private final String id;
    private final String status;
    private final String intygsId;

    public SigneringsBiljett(String id, String status, String intygsId) {
        this.id = id;
        this.status = status;
        this.intygsId = intygsId;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getIntygsId() {
        return intygsId;
    }
}
