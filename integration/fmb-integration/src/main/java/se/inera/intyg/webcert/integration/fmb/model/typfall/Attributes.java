/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.integration.fmb.model.typfall;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    public Optional<Fmbtillstand> getOptionalFmbtillstand() {
        return Optional.ofNullable(fmbtillstand);
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

    public Optional<Rekommenderadsjukskrivning> getOptionalRekommenderadsjukskrivning() {
        return Optional.ofNullable(rekommenderadsjukskrivning);
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
