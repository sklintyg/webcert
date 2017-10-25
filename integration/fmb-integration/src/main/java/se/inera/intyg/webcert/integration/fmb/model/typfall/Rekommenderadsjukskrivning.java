
package se.inera.intyg.webcert.integration.fmb.model.typfall;

import java.util.HashMap;
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
    "maximalsjukskrivningsenhet",
    "maximalsjukskrivningsgrad",
    "maximalsjukskrivningstid",
    "typfalletsrekommenderadesjukskrivning"
})
public class Rekommenderadsjukskrivning {

    @JsonProperty("maximalsjukskrivningsenhet")
    private String maximalsjukskrivningsenhet;
    @JsonProperty("maximalsjukskrivningsgrad")
    private Kod maximalsjukskrivningsgrad;
    @JsonProperty("maximalsjukskrivningstid")
    private String maximalsjukskrivningstid;
    @JsonProperty("typfalletsrekommenderadesjukskrivning")
    private String typfalletsrekommenderadesjukskrivning;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("maximalsjukskrivningsenhet")
    public String getMaximalsjukskrivningsenhet() {
        return maximalsjukskrivningsenhet;
    }

    @JsonProperty("maximalsjukskrivningsenhet")
    public void setMaximalsjukskrivningsenhet(String maximalsjukskrivningsenhet) {
        this.maximalsjukskrivningsenhet = maximalsjukskrivningsenhet;
    }

    @JsonProperty("maximalsjukskrivningsgrad")
    public Kod getMaximalsjukskrivningsgrad() {
        return maximalsjukskrivningsgrad;
    }

    @JsonProperty("maximalsjukskrivningsgrad")
    public void setMaximalsjukskrivningsgrad(Kod maximalsjukskrivningsgrad) {
        this.maximalsjukskrivningsgrad = maximalsjukskrivningsgrad;
    }

    @JsonProperty("maximalsjukskrivningstid")
    public String getMaximalsjukskrivningstid() {
        return maximalsjukskrivningstid;
    }

    @JsonProperty("maximalsjukskrivningstid")
    public void setMaximalsjukskrivningstid(String maximalsjukskrivningstid) {
        this.maximalsjukskrivningstid = maximalsjukskrivningstid;
    }

    @JsonProperty("typfalletsrekommenderadesjukskrivning")
    public String getTypfalletsrekommenderadesjukskrivning() {
        return typfalletsrekommenderadesjukskrivning;
    }

    @JsonProperty("typfalletsrekommenderadesjukskrivning")
    public void setTypfalletsrekommenderadesjukskrivning(String typfalletsrekommenderadesjukskrivning) {
        this.typfalletsrekommenderadesjukskrivning = typfalletsrekommenderadesjukskrivning;
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
