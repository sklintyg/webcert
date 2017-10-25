
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
import se.inera.intyg.webcert.integration.fmb.model.FmInfo;
import se.inera.intyg.webcert.integration.fmb.model.Links;
import se.inera.intyg.webcert.integration.fmb.model.Meta;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "data",
    "included",
    "links",
    "meta"
})
public class Typfall implements FmInfo {

    @JsonProperty("data")
    private List<TypfallData> data = null;
    @JsonProperty("included")
    private List<Included> included = null;
    @JsonProperty("links")
    private Links links;
    @JsonProperty("meta")
    private Meta meta;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("data")
    public List<TypfallData> getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(List<TypfallData> data) {
        this.data = data;
    }

    @JsonProperty("included")
    public List<Included> getIncluded() {
        return included;
    }

    @JsonProperty("included")
    public void setIncluded(List<Included> included) {
        this.included = included;
    }

    @JsonProperty("links")
    public Links getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(Links links) {
        this.links = links;
    }

    @JsonProperty("meta")
    public Meta getMeta() {
        return meta;
    }

    @JsonProperty("meta")
    public void setMeta(Meta meta) {
        this.meta = meta;
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
