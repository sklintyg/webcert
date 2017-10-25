
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
    "samsjuklighetsrubrik"
})
public class Urvalsamsjuklighet {

    @JsonProperty("diagnoskod")
    private List<Kod> diagnoskod = null;
    @JsonProperty("samsjuklighetsrubrik")
    private String samsjuklighetsrubrik;
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

    @JsonProperty("samsjuklighetsrubrik")
    public String getSamsjuklighetsrubrik() {
        return samsjuklighetsrubrik;
    }

    @JsonProperty("samsjuklighetsrubrik")
    public void setSamsjuklighetsrubrik(String samsjuklighetsrubrik) {
        this.samsjuklighetsrubrik = samsjuklighetsrubrik;
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
