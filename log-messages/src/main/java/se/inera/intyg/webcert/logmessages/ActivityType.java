package se.inera.intyg.webcert.logmessages;

public enum ActivityType {

    READ("Läsa"),
    CREATE("Skriva"),
    UPDATE("Skriva"),
    SIGN("Signera"),
    DELETE("Radera"),
    PRINT("Utskrift"),
    REVOKE("Radera"),
    SEND("Utskrift"),
    EMERGENCY_ACCESS("Nödöppning");

    private String type;

    ActivityType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
