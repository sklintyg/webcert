
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
    "kod",
    "status"
})
public class Urvalatgard {

    @JsonProperty("kod")
    private List<Kod> kod = null;
    @JsonProperty("status")
    private Kod status;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("kod")
    public List<Kod> getKod() {
        return kod;
    }

    @JsonProperty("kod")
    public void setKod(List<Kod> kod) {
        this.kod = kod;
    }

    @JsonProperty("status")
    public Kod getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(Kod status) {
        this.status = status;
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
