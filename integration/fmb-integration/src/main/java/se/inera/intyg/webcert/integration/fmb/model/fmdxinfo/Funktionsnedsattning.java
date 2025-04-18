/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.integration.fmb.model.fmdxinfo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.inera.intyg.webcert.integration.fmb.model.Kod;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "funktionsnedsattningsbeskrivning",
    "kompletterandekod",
    "centralkod"
})
public class Funktionsnedsattning implements FmxBeskrivning {

    @JsonProperty("kompletterandekod")
    private List<Kod> kompletterandekod = null;
    @JsonProperty("centralkod")
    private List<Kod> centralkod = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonProperty("funktionsnedsattningsbeskrivning")
    private String beskrivning;


    @JsonProperty("kompletterandekod")
    @Override
    public List<Kod> getKompletterandekod() {
        return kompletterandekod != null ? kompletterandekod : Lists.newArrayList();
    }

    @JsonProperty("kompletterandekod")
    public void setKompletterandekod(List<Kod> kompletterandekod) {
        this.kompletterandekod = kompletterandekod;
    }

    @JsonProperty("centralkod")
    @Override
    public List<Kod> getCentralkod() {
        return centralkod != null ? centralkod : Lists.newArrayList();
    }

    @JsonProperty("centralkod")
    public void setCentralkod(List<Kod> centralkod) {
        this.centralkod = centralkod;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonProperty("funktionsnedsattningsbeskrivning")
    @Override
    public String getBeskrivning() {
        return beskrivning;
    }

    @JsonProperty("funktionsnedsattningsbeskrivning")
    public void setBeskrivning(String beskrivning) {
        this.beskrivning = beskrivning;
    }

}
