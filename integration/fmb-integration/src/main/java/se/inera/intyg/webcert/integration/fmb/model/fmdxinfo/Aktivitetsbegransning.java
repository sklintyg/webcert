
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
    "aktivitetsbegransningsbeskrivning",
    "kompletterandekod",
    "centralkod"
})
public class Aktivitetsbegransning {

    @JsonProperty("aktivitetsbegransningsbeskrivning")
    private String aktivitetsbegransningsbeskrivning;
    @JsonProperty("kompletterandekod")
    private List<Kod> kompletterandekod = null;
    @JsonProperty("centralkod")
    private List<Kod> centralkod = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("aktivitetsbegransningsbeskrivning")
    public String getAktivitetsbegransningsbeskrivning() {
        return aktivitetsbegransningsbeskrivning;
    }

    @JsonProperty("aktivitetsbegransningsbeskrivning")
    public void setAktivitetsbegransningsbeskrivning(String aktivitetsbegransningsbeskrivning) {
        this.aktivitetsbegransningsbeskrivning = aktivitetsbegransningsbeskrivning;
    }

    @JsonProperty("kompletterandekod")
    public List<Kod> getKompletterandekod() {
        return kompletterandekod;
    }

    @JsonProperty("kompletterandekod")
    public void setKompletterandekod(List<Kod> kompletterandekod) {
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
