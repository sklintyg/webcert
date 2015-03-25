package se.inera.log.messages;

public enum ActivityType {

    READ("Läsa"),
    CREATE("Skapa"),
    UPDATE("Spara"),
    SIGN("Signera"),
    DELETE("Radera"),
    PRINT("Utskrift"),
    REVOKE("Återkalla"),
    SEND("SkickaTillMottagare"),
    EMERGENCY_ACCESS("Nödöppning");

    private String type;

    private ActivityType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
