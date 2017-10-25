
package se.inera.intyg.webcert.integration.fmb.model.typfall;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "fmbtillstand",
    "id",
    "metadata",
    "rekommenderadsjukskrivning",
    "typfallsmening"
})
public class Attributes {

    @JsonProperty("fmbtillstand")
    private Fmbtillstand fmbtillstand;
    @JsonProperty("id")
    private String id;
    @JsonProperty("metadata")
    private Metadata metadata;
    @JsonProperty("rekommenderadsjukskrivning")
    private Rekommenderadsjukskrivning rekommenderadsjukskrivning;
    @JsonProperty("typfallsmening")
    private String typfallsmening;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("fmbtillstand")
    public Fmbtillstand getFmbtillstand() {
        return fmbtillstand;
    }

    @JsonProperty("fmbtillstand")
    public void setFmbtillstand(Fmbtillstand fmbtillstand) {
        this.fmbtillstand = fmbtillstand;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("metadata")
    public Metadata getMetadata() {
        return metadata;
    }

    @JsonProperty("metadata")
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @JsonProperty("rekommenderadsjukskrivning")
    public Rekommenderadsjukskrivning getRekommenderadsjukskrivning() {
        return rekommenderadsjukskrivning;
    }

    @JsonProperty("rekommenderadsjukskrivning")
    public void setRekommenderadsjukskrivning(Rekommenderadsjukskrivning rekommenderadsjukskrivning) {
        this.rekommenderadsjukskrivning = rekommenderadsjukskrivning;
    }

    @JsonProperty("typfallsmening")
    public String getTypfallsmening() {
        return typfallsmening;
    }

    @JsonProperty("typfallsmening")
    public void setTypfallsmening(String typfallsmening) {
        this.typfallsmening = typfallsmening;
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
