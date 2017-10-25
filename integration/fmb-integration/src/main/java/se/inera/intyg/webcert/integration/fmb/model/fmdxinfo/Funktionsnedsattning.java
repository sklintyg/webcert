
package se.inera.intyg.webcert.integration.fmb.model.fmdxinfo;

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
    "funktionsnedsattningsbeskrivning",
    "kompletterandekod",
    "centralkod"
})
public class Funktionsnedsattning {

    @JsonProperty("funktionsnedsattningsbeskrivning")
    private String funktionsnedsattningsbeskrivning;
    @JsonProperty("kompletterandekod")
    private List<Object> kompletterandekod = null;
    @JsonProperty("centralkod")
    private List<Kod> centralkod = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("funktionsnedsattningsbeskrivning")
    public String getFunktionsnedsattningsbeskrivning() {
        return funktionsnedsattningsbeskrivning;
    }

    @JsonProperty("funktionsnedsattningsbeskrivning")
    public void setFunktionsnedsattningsbeskrivning(String funktionsnedsattningsbeskrivning) {
        this.funktionsnedsattningsbeskrivning = funktionsnedsattningsbeskrivning;
    }

    @JsonProperty("kompletterandekod")
    public List<Object> getKompletterandekod() {
        return kompletterandekod;
    }

    @JsonProperty("kompletterandekod")
    public void setKompletterandekod(List<Object> kompletterandekod) {
        this.kompletterandekod = kompletterandekod;
    }

    @JsonProperty("centralkod")
    public List<Kod> getCentralkod() {
        return centralkod;
    }

    @JsonProperty("centralkod")
    public void setCentralkod(List<Kod> centralkod) {
        this.centralkod = centralkod;
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
