package se.inera.webcert.modules.api.dto;

public class Vardenhet {

    private String hsaId;
    
    private String namn;
    
    private Vardgivare vardgivare;
    
    public Vardenhet() {
        
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public Vardgivare getVardgivare() {
        return vardgivare;
    }

    public void setVardgivare(Vardgivare vardgivare) {
        this.vardgivare = vardgivare;
    }

}
