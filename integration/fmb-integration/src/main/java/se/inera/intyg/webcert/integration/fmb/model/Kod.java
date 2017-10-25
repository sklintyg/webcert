
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
    "beskrivning",
    "kod",
    "kodsystem",
    "kodsystemnamn",
    "kodsystemversion"
})
public class Kod {

    @JsonProperty("beskrivning")
    private String beskrivning;
    @JsonProperty("kod")
    private String kod;
    @JsonProperty("kodsystem")
    private String kodsystem;
    @JsonProperty("kodsystemnamn")
    private String kodsystemnamn;
    @JsonProperty("kodsystemversion")
    private String kodsystemversion;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("beskrivning")
    public String getBeskrivning() {
        return beskrivning;
    }

    @JsonProperty("beskrivning")
    public void setBeskrivning(String beskrivning) {
        this.beskrivning = beskrivning;
    }

    @JsonProperty("kod")
    public String getKod() {
        return kod;
    }

    @JsonProperty("kod")
    public void setKod(String kod) {
        this.kod = kod;
    }

    @JsonProperty("kodsystem")
    public String getKodsystem() {
        return kodsystem;
    }

    @JsonProperty("kodsystem")
    public void setKodsystem(String kodsystem) {
        this.kodsystem = kodsystem;
    }

    @JsonProperty("kodsystemnamn")
    public String getKodsystemnamn() {
        return kodsystemnamn;
    }

    @JsonProperty("kodsystemnamn")
    public void setKodsystemnamn(String kodsystemnamn) {
        this.kodsystemnamn = kodsystemnamn;
    }

    @JsonProperty("kodsystemversion")
    public String getKodsystemversion() {
        return kodsystemversion;
    }

    @JsonProperty("kodsystemversion")
    public void setKodsystemversion(String kodsystemversion) {
        this.kodsystemversion = kodsystemversion;
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
