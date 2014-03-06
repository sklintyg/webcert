package se.inera.webcert.web.controller.moduleapi.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"namn", "hsaId", "fragaSvar", "intyg"})
public class VardgivareStats {
    
    @JsonProperty("fragaSvar")
    private long totalOhanteradeFragaSvar;
    
    @JsonProperty("intyg")
    private long totalOsigneradeIntyg;
    
    @JsonProperty("namn")
    private String namn;
    
    @JsonProperty("hsaId")
    private String hsaId;
    
    private List<VardenhetStats> vardenheter = new ArrayList<VardenhetStats>();
    
    public VardgivareStats(@JsonProperty("namn") String namn, @JsonProperty("hsaId") String hsaId) {
        super();
        this.namn = namn;
        this.hsaId = hsaId;
    }

    public long getTotalOhanteradeFragaSvar() {
        return totalOhanteradeFragaSvar;
    }

    public void setTotalOhanteradeFragaSvar(long totalOhanteradeFragaSvar) {
        this.totalOhanteradeFragaSvar = totalOhanteradeFragaSvar;
    }

    public long getTotalOsigneradeIntyg() {
        return totalOsigneradeIntyg;
    }

    public void setTotalOsigneradeIntyg(long totalOsigneradeIntyg) {
        this.totalOsigneradeIntyg = totalOsigneradeIntyg;
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

    public List<VardenhetStats> getVardenheter() {
        return vardenheter;
    }

    public void setVardenheter(List<VardenhetStats> vardenheter) {
        this.vardenheter = vardenheter;
    }

    @Override
    @JsonIgnore
    public String toString() {
        return "VardgivareStats [totalOhanteradeFragaSvar=" + totalOhanteradeFragaSvar + ", totalOsigneradeIntyg="
                + totalOsigneradeIntyg + ", namn=" + namn + ", hsaId=" + hsaId + ", vardenheter=" + vardenheter + "]";
    }
}
