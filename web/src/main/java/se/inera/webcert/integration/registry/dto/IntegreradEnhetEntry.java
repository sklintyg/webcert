package se.inera.webcert.integration.registry.dto;

public class IntegreradEnhetEntry {

    private String enhetsId;
    
    private String enhetsNamn;
    
    private String vardgivareId;
    
    private String vardgivareNamn;
        
    public IntegreradEnhetEntry(String enhetsId, String vardgivareId) {
        super();
        this.enhetsId = enhetsId;
        this.vardgivareId = vardgivareId;
    }

    public IntegreradEnhetEntry(String enhetsId, String enhetsNamn, String vardgivareId, String vardgivareNamn) {
        super();
        this.enhetsId = enhetsId;
        this.enhetsNamn = enhetsNamn;
        this.vardgivareId = vardgivareId;
        this.vardgivareNamn = vardgivareNamn;
    }

    public String getEnhetsId() {
        return enhetsId;
    }

    public void setEnhetsId(String enhetsId) {
        this.enhetsId = enhetsId;
    }

    public String getEnhetsNamn() {
        return enhetsNamn;
    }

    public void setEnhetsNamn(String enhetsNamn) {
        this.enhetsNamn = enhetsNamn;
    }

    public String getVardgivareId() {
        return vardgivareId;
    }

    public void setVardgivareId(String vardgivareId) {
        this.vardgivareId = vardgivareId;
    }

    public String getVardgivareNamn() {
        return vardgivareNamn;
    }

    public void setVardgivareNamn(String vardgivareNamn) {
        this.vardgivareNamn = vardgivareNamn;
    }
    
}
