package se.inera.log.messages;

public enum ActivityType {
    
    READ("Läsa"),
    WRITE("Skriva"),
    SIGN("Signera"),
    DELETE("Radera"),
    PRINT("Utskrift"),
    EMERGENCY_ACCESS("Nödöppning");
    
    private String type;
    
    private ActivityType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
}
