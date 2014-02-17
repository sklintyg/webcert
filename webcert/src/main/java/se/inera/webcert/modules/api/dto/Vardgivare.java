package se.inera.webcert.modules.api.dto;

public class Vardgivare {

    private String hsaId;

    private String namn;

    public Vardgivare() {

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

}
