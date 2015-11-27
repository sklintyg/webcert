package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"namn", "id", "vardenheter" })
public class VardgivareStats {

    @JsonProperty("namn")
    private String namn;

    @JsonProperty("id")
    private String hsaId;

    private List<VardenhetStats> vardenheter = new ArrayList<>();

    public VardgivareStats(@JsonProperty("namn") String namn, @JsonProperty("id") String hsaId) {
        super();
        this.namn = namn;
        this.hsaId = hsaId;
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
        return "VardgivareStats [namn=" + namn + ", hsaId=" + hsaId + ", vardenheter=" + vardenheter + "]";
    }

}
