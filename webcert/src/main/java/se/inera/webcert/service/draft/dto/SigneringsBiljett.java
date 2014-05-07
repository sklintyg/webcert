package se.inera.webcert.service.draft.dto;

public class SigneringsBiljett {

    private final String id;
    private final String status;
    private final String intygsId;
    private final String hash;

    public SigneringsBiljett(String id, String status, String intygsId, String hash) {
        this.id = id;
        this.status = status;
        this.intygsId = intygsId;
        this.hash = hash;
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

    public String getHash() {
        return hash;
    }

    public SigneringsBiljett withStatus(String status) {
        return new SigneringsBiljett(id, status, intygsId, hash);
    }
}
