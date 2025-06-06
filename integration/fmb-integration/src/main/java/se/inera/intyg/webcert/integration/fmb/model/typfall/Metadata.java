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
package se.inera.intyg.webcert.integration.fmb.model.typfall;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.inera.intyg.webcert.integration.fmb.model.Giltighetsperiod;
import se.inera.intyg.webcert.integration.fmb.model.Kod;
import se.inera.intyg.webcert.integration.fmb.model.Sjukdomsgrupp;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "beskrivning",
    "giltighetsperiod",
    "gradavstyrning",
    "id",
    "malgrupp",
    "nyckelord",
    "publiceringsdatum",
    "senastegenomgang",
    "senastuppdaterad",
    "sjukdomsgrupp",
    "skapaddatum",
    "sprak",
    "status",
    "syfte",
    "titel",
    "typavdokument",
    "version"
})
public class Metadata {

    @JsonProperty("beskrivning")
    private String beskrivning;
    @JsonProperty("giltighetsperiod")
    private Giltighetsperiod giltighetsperiod;
    @JsonProperty("gradavstyrning")
    private Kod gradavstyrning;
    @JsonProperty("id")
    private String id;
    @JsonProperty("malgrupp")
    private List<Kod> malgrupp = null;
    @JsonProperty("nyckelord")
    private List<Kod> nyckelord = null;
    @JsonProperty("publiceringsdatum")
    private String publiceringsdatum;
    @JsonProperty("senastegenomgang")
    private String senastegenomgang;
    @JsonProperty("senastuppdaterad")
    private String senastuppdaterad;
    @JsonProperty("sjukdomsgrupp")
    private Sjukdomsgrupp sjukdomsgrupp;
    @JsonProperty("skapaddatum")
    private String skapaddatum;
    @JsonProperty("sprak")
    private String sprak;
    @JsonProperty("status")
    private Kod status;
    @JsonProperty("syfte")
    private String syfte;
    @JsonProperty("titel")
    private String titel;
    @JsonProperty("typavdokument")
    private Kod typavdokument;
    @JsonProperty("version")
    private String version;
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

    @JsonProperty("giltighetsperiod")
    public Giltighetsperiod getGiltighetsperiod() {
        return giltighetsperiod;
    }

    @JsonProperty("giltighetsperiod")
    public void setGiltighetsperiod(Giltighetsperiod giltighetsperiod) {
        this.giltighetsperiod = giltighetsperiod;
    }

    @JsonProperty("gradavstyrning")
    public Kod getGradavstyrning() {
        return gradavstyrning;
    }

    @JsonProperty("gradavstyrning")
    public void setGradavstyrning(Kod gradavstyrning) {
        this.gradavstyrning = gradavstyrning;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("malgrupp")
    public List<Kod> getMalgrupp() {
        return malgrupp;
    }

    @JsonProperty("malgrupp")
    public void setMalgrupp(List<Kod> malgrupp) {
        this.malgrupp = malgrupp;
    }

    @JsonProperty("nyckelord")
    public List<Kod> getNyckelord() {
        return nyckelord;
    }

    @JsonProperty("nyckelord")
    public void setNyckelord(List<Kod> nyckelord) {
        this.nyckelord = nyckelord;
    }

    @JsonProperty("publiceringsdatum")
    public String getPubliceringsdatum() {
        return publiceringsdatum;
    }

    @JsonProperty("publiceringsdatum")
    public void setPubliceringsdatum(String publiceringsdatum) {
        this.publiceringsdatum = publiceringsdatum;
    }

    @JsonProperty("senastegenomgang")
    public String getSenastegenomgang() {
        return senastegenomgang;
    }

    @JsonProperty("senastegenomgang")
    public void setSenastegenomgang(String senastegenomgang) {
        this.senastegenomgang = senastegenomgang;
    }

    @JsonProperty("senastuppdaterad")
    public String getSenastuppdaterad() {
        return senastuppdaterad;
    }

    @JsonProperty("senastuppdaterad")
    public void setSenastuppdaterad(String senastuppdaterad) {
        this.senastuppdaterad = senastuppdaterad;
    }

    @JsonProperty("sjukdomsgrupp")
    public Sjukdomsgrupp getSjukdomsgrupp() {
        return sjukdomsgrupp;
    }

    @JsonProperty("sjukdomsgrupp")
    public void setSjukdomsgrupp(Sjukdomsgrupp sjukdomsgrupp) {
        this.sjukdomsgrupp = sjukdomsgrupp;
    }

    @JsonProperty("skapaddatum")
    public String getSkapaddatum() {
        return skapaddatum;
    }

    @JsonProperty("skapaddatum")
    public void setSkapaddatum(String skapaddatum) {
        this.skapaddatum = skapaddatum;
    }

    @JsonProperty("sprak")
    public String getSprak() {
        return sprak;
    }

    @JsonProperty("sprak")
    public void setSprak(String sprak) {
        this.sprak = sprak;
    }

    @JsonProperty("status")
    public Kod getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(Kod status) {
        this.status = status;
    }

    @JsonProperty("syfte")
    public String getSyfte() {
        return syfte;
    }

    @JsonProperty("syfte")
    public void setSyfte(String syfte) {
        this.syfte = syfte;
    }

    @JsonProperty("titel")
    public String getTitel() {
        return titel;
    }

    @JsonProperty("titel")
    public void setTitel(String titel) {
        this.titel = titel;
    }

    @JsonProperty("typavdokument")
    public Kod getTypavdokument() {
        return typavdokument;
    }

    @JsonProperty("typavdokument")
    public void setTypavdokument(Kod typavdokument) {
        this.typavdokument = typavdokument;
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
