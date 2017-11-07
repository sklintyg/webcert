package se.inera.intyg.webcert.integration.tak.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "platform",
        "environment",
        "snapshotTime"
})
public class ConnectionPoint {
    @JsonProperty("id")
    private String id;
    @JsonProperty("platform")
    private String platform;
    @JsonProperty("environment")
    private String environment;
    @JsonProperty("snapshotTime")
    private String snapshotTime;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("platform")
    public String getPlatform() {
        return platform;
    }

    @JsonProperty("environment")
    public String getEnvironment() {
        return environment;
    }

    @JsonProperty("snapshotTime")
    public String getSnapshotTime() {
        return snapshotTime;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("platform")
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @JsonProperty("environment")
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @JsonProperty("snapshotTime")
    public void setSnapshotTime(String snapshotTime) {
        this.snapshotTime = snapshotTime;
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
