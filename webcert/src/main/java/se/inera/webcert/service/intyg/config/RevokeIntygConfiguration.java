package se.inera.webcert.service.intyg.config;

public class RevokeIntygConfiguration {

    private String revokeMessage;
    
    public RevokeIntygConfiguration() {
        
    }
    
    public RevokeIntygConfiguration(String revokeMessage) {
        super();
        this.revokeMessage = revokeMessage;
    }

    public String getRevokeMessage() {
        return revokeMessage;
    }

    public void setRevokeMessage(String revokeMessage) {
        this.revokeMessage = revokeMessage;
    }
    
}
