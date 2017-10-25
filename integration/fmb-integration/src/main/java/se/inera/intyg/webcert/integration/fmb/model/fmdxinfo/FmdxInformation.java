
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
import se.inera.intyg.webcert.integration.fmb.model.FmInfo;
import se.inera.intyg.webcert.integration.fmb.model.Links;
import se.inera.intyg.webcert.integration.fmb.model.Meta;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "meta",
    "links",
    "data"
})
public class FmdxInformation implements FmInfo {

    @JsonProperty("meta")
    private Meta meta;
    @JsonProperty("links")
    private Links links;
    @JsonProperty("data")
    private List<FmdxData> data = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @Override
    @JsonProperty("meta")
    public Meta getMeta() {
        return meta;
    }

    @JsonProperty("meta")
    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    @JsonProperty("links")
    public Links getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(Links links) {
        this.links = links;
    }

    @JsonProperty("data")
    public List<FmdxData> getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(List<FmdxData> data) {
        this.data = data;
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
