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

import java.util.HashMap;
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
    "maximalsjukskrivningsenhet",
    "maximalsjukskrivningsgrad",
    "maximalsjukskrivningstid",
    "typfalletsrekommenderadesjukskrivning"
})
public class Rekommenderadsjukskrivning {

    @JsonProperty("maximalsjukskrivningsenhet")
    private String maximalsjukskrivningsenhet;
    @JsonProperty("maximalsjukskrivningsgrad")
    private Kod maximalsjukskrivningsgrad;
    @JsonProperty("maximalsjukskrivningstid")
    private String maximalsjukskrivningstid;
    @JsonProperty("typfalletsrekommenderadesjukskrivning")
    private String typfalletsrekommenderadesjukskrivning;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("maximalsjukskrivningsenhet")
    public String getMaximalsjukskrivningsenhet() {
        return maximalsjukskrivningsenhet;
    }

    @JsonProperty("maximalsjukskrivningsenhet")
    public void setMaximalsjukskrivningsenhet(String maximalsjukskrivningsenhet) {
        this.maximalsjukskrivningsenhet = maximalsjukskrivningsenhet;
    }

    @JsonProperty("maximalsjukskrivningsgrad")
    public Kod getMaximalsjukskrivningsgrad() {
        return maximalsjukskrivningsgrad;
    }

    @JsonProperty("maximalsjukskrivningsgrad")
    public void setMaximalsjukskrivningsgrad(Kod maximalsjukskrivningsgrad) {
        this.maximalsjukskrivningsgrad = maximalsjukskrivningsgrad;
    }

    @JsonProperty("maximalsjukskrivningstid")
    public String getMaximalsjukskrivningstid() {
        return maximalsjukskrivningstid;
    }

    @JsonProperty("maximalsjukskrivningstid")
    public void setMaximalsjukskrivningstid(String maximalsjukskrivningstid) {
        this.maximalsjukskrivningstid = maximalsjukskrivningstid;
    }

    @JsonProperty("typfalletsrekommenderadesjukskrivning")
    public String getTypfalletsrekommenderadesjukskrivning() {
        return typfalletsrekommenderadesjukskrivning;
    }

    @JsonProperty("typfalletsrekommenderadesjukskrivning")
    public void setTypfalletsrekommenderadesjukskrivning(String typfalletsrekommenderadesjukskrivning) {
        this.typfalletsrekommenderadesjukskrivning = typfalletsrekommenderadesjukskrivning;
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
