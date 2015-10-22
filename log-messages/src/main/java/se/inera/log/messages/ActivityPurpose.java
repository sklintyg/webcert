package se.inera.log.messages;

public enum ActivityPurpose {

    CARE_TREATMENT("Vård och behandling");

    private String type;

    ActivityPurpose(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
