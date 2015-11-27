package se.inera.intyg.webcert.web.service.dto;

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

    public static Vardgivare create(se.inera.webcert.hsa.model.Vardgivare hsaVardgivare) {
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setHsaId(hsaVardgivare.getId());
        vardgivare.setNamn(hsaVardgivare.getNamn());
        return vardgivare;
    }
}
