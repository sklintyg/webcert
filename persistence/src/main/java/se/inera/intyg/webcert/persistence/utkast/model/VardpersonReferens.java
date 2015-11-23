package se.inera.intyg.webcert.persistence.utkast.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class VardpersonReferens {
    @Column(name = "HSAID")
    private String hsaId;

    @Column(name = "NAMN")
    private String namn;

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
