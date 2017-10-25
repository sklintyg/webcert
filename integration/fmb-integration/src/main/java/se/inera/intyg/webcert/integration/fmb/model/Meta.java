
package se.inera.intyg.webcert.integration.fmb.model;

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
    "buildnumber",
    "buildtimestamp",
    "version"
})
public class Meta {

    @JsonProperty("buildnumber")
    private String buildnumber;
    @JsonProperty("buildtimestamp")
    private String buildtimestamp;
    @JsonProperty("version")
    private String version;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("buildnumber")
    public String getBuildnumber() {
        return buildnumber;
    }

    @JsonProperty("buildnumber")
    public void setBuildnumber(String buildnumber) {
        this.buildnumber = buildnumber;
    }

    @JsonProperty("buildtimestamp")
    public String getBuildtimestamp() {
        return buildtimestamp;
    }

    @JsonProperty("buildtimestamp")
    public void setBuildtimestamp(String buildtimestamp) {
        this.buildtimestamp = buildtimestamp;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
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
