package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"namn", "id", "fragaSvar", "intyg" })
public class VardenhetStats {

    @JsonProperty("fragaSvar")
    private long ohanteradeFragaSvar;

    @JsonProperty("intyg")
    private long osigneradeIntyg;

    @JsonProperty("namn")
    private String namn;

    @JsonProperty("id")
    private String hsaId;

    public VardenhetStats(@JsonProperty("namn") String namn, @JsonProperty("id") String hsaId) {
        super();
        this.namn = namn;
        this.hsaId = hsaId;
    }

    public long getOhanteradeFragaSvar() {
        return ohanteradeFragaSvar;
    }

    public void setOhanteradeFragaSvar(long ohanteradeFragaSvar) {
        this.ohanteradeFragaSvar = ohanteradeFragaSvar;
    }

    public long getOsigneradeIntyg() {
        return osigneradeIntyg;
    }

    public void setOsigneradeIntyg(long osigneradeIntyg) {
        this.osigneradeIntyg = osigneradeIntyg;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    @Override
    @JsonIgnore
    public String toString() {
        return "VardenhetStats [ohanteradeFragaSvar=" + ohanteradeFragaSvar + ", osigneradeIntyg=" + osigneradeIntyg
                + ", namn=" + namn + ", hsaId=" + hsaId + "]";
    }

}
