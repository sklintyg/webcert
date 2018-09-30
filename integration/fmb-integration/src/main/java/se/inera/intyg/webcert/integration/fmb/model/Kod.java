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
package se.inera.intyg.webcert.integration.fmb.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    public Optional<String> getOptionalKod() {
        return Optional.of(kod);
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Kod kod1 = (Kod) o;

        return new EqualsBuilder()
                .append(beskrivning, kod1.beskrivning)
                .append(kod, kod1.kod)
                .append(kodsystem, kod1.kodsystem)
                .append(kodsystemnamn, kod1.kodsystemnamn)
                .append(kodsystemversion, kod1.kodsystemversion)
                .append(additionalProperties, kod1.additionalProperties)
                .isEquals();
    }

    // CHECKSTYLE:OFF MagicNumber
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(beskrivning)
                .append(kod)
                .append(kodsystem)
                .append(kodsystemnamn)
                .append(kodsystemversion)
                .append(additionalProperties)
                .toHashCode();
    }
    // CHECKSTYLE:ON MagicNumber

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("beskrivning", beskrivning)
                .append("kod", kod)
                .append("kodsystem", kodsystem)
                .append("kodsystemnamn", kodsystemnamn)
                .append("kodsystemversion", kodsystemversion)
                .append("additionalProperties", additionalProperties)
                .toString();
    }
}
