
package se.inera.intyg.webcert.integration.fmb.model.typfall;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import se.inera.intyg.webcert.integration.fmb.model.Kod;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "diagnoskod",
    "tillstandsmening",
    "urvalarbetsbelastning",
    "urvalatgard",
    "urvalkomplicerandefaktor",
    "urvalsamsjuklighet",
    "urvalsjukdomsforlopp",
    "urvalsvarighetsgrad"
})
public class Fmbtillstand {

    @JsonProperty("diagnoskod")
    private List<Kod> diagnoskod = null;
    @JsonProperty("tillstandsmening")
    private String tillstandsmening;
    @JsonProperty("urvalarbetsbelastning")
    private List<Urvalarbetsbelastning> urvalarbetsbelastning = null;
    @JsonProperty("urvalatgard")
    private List<Urvalatgard> urvalatgard = null;
    @JsonProperty("urvalkomplicerandefaktor")
    private List<Urvalkomplicerandefaktor> urvalkomplicerandefaktor = null;
    @JsonProperty("urvalsamsjuklighet")
    private List<List<Urvalsamsjuklighet>> urvalsamsjuklighet = null;
    @JsonProperty("urvalsjukdomsforlopp")
    private List<Urvalsjukdomsforlopp> urvalsjukdomsforlopp = null;
    @JsonProperty("urvalsvarighetsgrad")
    private List<Urvalsvarighetsgrad> urvalsvarighetsgrad = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("diagnoskod")
    public List<Kod> getDiagnoskod() {
        return diagnoskod;
    }

    @JsonProperty("diagnoskod")
    public void setDiagnoskod(List<Kod> diagnoskod) {
        this.diagnoskod = diagnoskod;
    }

    @JsonProperty("tillstandsmening")
    public String getTillstandsmening() {
        return tillstandsmening;
    }

    @JsonProperty("tillstandsmening")
    public void setTillstandsmening(String tillstandsmening) {
        this.tillstandsmening = tillstandsmening;
    }

    @JsonProperty("urvalarbetsbelastning")
    public List<Urvalarbetsbelastning> getUrvalarbetsbelastning() {
        return urvalarbetsbelastning;
    }

    @JsonProperty("urvalarbetsbelastning")
    public void setUrvalarbetsbelastning(List<Urvalarbetsbelastning> urvalarbetsbelastning) {
        this.urvalarbetsbelastning = urvalarbetsbelastning;
    }

    @JsonProperty("urvalatgard")
    public List<Urvalatgard> getUrvalatgard() {
        return urvalatgard;
    }

    @JsonProperty("urvalatgard")
    public void setUrvalatgard(List<Urvalatgard> urvalatgard) {
        this.urvalatgard = urvalatgard;
    }

    @JsonProperty("urvalkomplicerandefaktor")
    public List<Urvalkomplicerandefaktor> getUrvalkomplicerandefaktor() {
        return urvalkomplicerandefaktor;
    }

    @JsonProperty("urvalkomplicerandefaktor")
    public void setUrvalkomplicerandefaktor(List<Urvalkomplicerandefaktor> urvalkomplicerandefaktor) {
        this.urvalkomplicerandefaktor = urvalkomplicerandefaktor;
    }

    @JsonProperty("urvalsamsjuklighet")
    public List<List<Urvalsamsjuklighet>> getUrvalsamsjuklighet() {
        return urvalsamsjuklighet;
    }

    @JsonProperty("urvalsamsjuklighet")
    public void setUrvalsamsjuklighet(List<List<Urvalsamsjuklighet>> urvalsamsjuklighet) {
        this.urvalsamsjuklighet = urvalsamsjuklighet;
    }

    @JsonProperty("urvalsjukdomsforlopp")
    public List<Urvalsjukdomsforlopp> getUrvalsjukdomsforlopp() {
        return urvalsjukdomsforlopp;
    }

    @JsonProperty("urvalsjukdomsforlopp")
    public void setUrvalsjukdomsforlopp(List<Urvalsjukdomsforlopp> urvalsjukdomsforlopp) {
        this.urvalsjukdomsforlopp = urvalsjukdomsforlopp;
    }

    @JsonProperty("urvalsvarighetsgrad")
    public List<Urvalsvarighetsgrad> getUrvalsvarighetsgrad() {
        return urvalsvarighetsgrad;
    }

    @JsonProperty("urvalsvarighetsgrad")
    public void setUrvalsvarighetsgrad(List<Urvalsvarighetsgrad> urvalsvarighetsgrad) {
        this.urvalsvarighetsgrad = urvalsvarighetsgrad;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
