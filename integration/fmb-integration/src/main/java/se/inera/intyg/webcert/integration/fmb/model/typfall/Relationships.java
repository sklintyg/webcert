
package se.inera.intyg.webcert.integration.fmb.model.typfall;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmdxInformation;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "forsakringsmedicinskdiagnosinformation"
})
public class Relationships {

    @JsonProperty("forsakringsmedicinskdiagnosinformation")
    private FmdxInformation forsakringsmedicinskdiagnosinformation;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("forsakringsmedicinskdiagnosinformation")
    public FmdxInformation getForsakringsmedicinskdiagnosinformation() {
        return forsakringsmedicinskdiagnosinformation;
    }

    @JsonProperty("forsakringsmedicinskdiagnosinformation")
    public void setForsakringsmedicinskdiagnosinformation(FmdxInformation forsakringsmedicinskdiagnosinformation) {
        this.forsakringsmedicinskdiagnosinformation = forsakringsmedicinskdiagnosinformation;
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
