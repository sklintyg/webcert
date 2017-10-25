/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import se.inera.intyg.webcert.integration.fmb.model.Giltighetsperiod;
import se.inera.intyg.webcert.integration.fmb.model.Kod;
import se.inera.intyg.webcert.integration.fmb.model.Sjukdomsgrupp;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "version",
    "id",
    "titel",
    "malgrupp",
    "nyckelord",
    "beskrivning",
    "typavdokument",
    "sjukdomsgrupp",
    "senastuppdaterad",
    "giltighetsperiod",
    "status",
    "diagnosrubrik",
    "forsakringsmedicinskinformation",
    "symtomprognosbehandling",
    "informationomrehabilitering",
    "diagnoskod",
    "aktivitetsbegransning",
    "funktionsnedsattning",
    "referens"
})
public class Attributes {

    @JsonProperty("version")
    private String version;
    @JsonProperty("id")
    private String id;
    @JsonProperty("titel")
    private String titel;
    @JsonProperty("malgrupp")
    private List<Kod> malgrupp = null;
    @JsonProperty("nyckelord")
    private List<Kod> nyckelord = null;
    @JsonProperty("beskrivning")
    private String beskrivning;
    @JsonProperty("typavdokument")
    private Kod typavdokument;
    @JsonProperty("sjukdomsgrupp")
    private Sjukdomsgrupp sjukdomsgrupp;
    @JsonProperty("senastuppdaterad")
    private String senastuppdaterad;
    @JsonProperty("giltighetsperiod")
    private Giltighetsperiod giltighetsperiod;
    @JsonProperty("status")
    private Kod status;
    @JsonProperty("diagnosrubrik")
    private String diagnosrubrik;
    @JsonProperty("forsakringsmedicinskinformation")
    private Markup forsakringsmedicinskinformation;
    @JsonProperty("symtomprognosbehandling")
    private Markup symtomprognosbehandling;
    @JsonProperty("informationomrehabilitering")
    private Informationomrehabilitering informationomrehabilitering;
    @JsonProperty("diagnoskod")
    private List<Kod> diagnoskod = null;
    @JsonProperty("aktivitetsbegransning")
    private Aktivitetsbegransning aktivitetsbegransning;
    @JsonProperty("funktionsnedsattning")
    private Funktionsnedsattning funktionsnedsattning;
    @JsonProperty("referens")
    private List<Referen> referens = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("titel")
    public String getTitel() {
        return titel;
    }

    @JsonProperty("titel")
    public void setTitel(String titel) {
        this.titel = titel;
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

    @JsonProperty("beskrivning")
    public String getBeskrivning() {
        return beskrivning;
    }

    @JsonProperty("beskrivning")
    public void setBeskrivning(String beskrivning) {
        this.beskrivning = beskrivning;
    }

    @JsonProperty("typavdokument")
    public Kod getTypavdokument() {
        return typavdokument;
    }

    @JsonProperty("typavdokument")
    public void setTypavdokument(Kod typavdokument) {
        this.typavdokument = typavdokument;
    }

    @JsonProperty("sjukdomsgrupp")
    public Sjukdomsgrupp getSjukdomsgrupp() {
        return sjukdomsgrupp;
    }

    @JsonProperty("sjukdomsgrupp")
    public void setSjukdomsgrupp(Sjukdomsgrupp sjukdomsgrupp) {
        this.sjukdomsgrupp = sjukdomsgrupp;
    }

    @JsonProperty("senastuppdaterad")
    public String getSenastuppdaterad() {
        return senastuppdaterad;
    }

    @JsonProperty("senastuppdaterad")
    public void setSenastuppdaterad(String senastuppdaterad) {
        this.senastuppdaterad = senastuppdaterad;
    }

    @JsonProperty("giltighetsperiod")
    public Giltighetsperiod getGiltighetsperiod() {
        return giltighetsperiod;
    }

    @JsonProperty("giltighetsperiod")
    public void setGiltighetsperiod(Giltighetsperiod giltighetsperiod) {
        this.giltighetsperiod = giltighetsperiod;
    }

    @JsonProperty("status")
    public Kod getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(Kod status) {
        this.status = status;
    }

    @JsonProperty("diagnosrubrik")
    public String getDiagnosrubrik() {
        return diagnosrubrik;
    }

    @JsonProperty("diagnosrubrik")
    public void setDiagnosrubrik(String diagnosrubrik) {
        this.diagnosrubrik = diagnosrubrik;
    }

    @JsonProperty("forsakringsmedicinskinformation")
    public Markup getForsakringsmedicinskinformation() {
        return forsakringsmedicinskinformation;
    }

    @JsonProperty("forsakringsmedicinskinformation")
    public void setForsakringsmedicinskinformation(Markup forsakringsmedicinskinformation) {
        this.forsakringsmedicinskinformation = forsakringsmedicinskinformation;
    }

    @JsonProperty("symtomprognosbehandling")
    public Markup getSymtomprognosbehandling() {
        return symtomprognosbehandling;
    }

    @JsonProperty("symtomprognosbehandling")
    public void setSymtomprognosbehandling(Markup symtomprognosbehandling) {
        this.symtomprognosbehandling = symtomprognosbehandling;
    }

    @JsonProperty("informationomrehabilitering")
    public Informationomrehabilitering getInformationomrehabilitering() {
        return informationomrehabilitering;
    }

    @JsonProperty("informationomrehabilitering")
    public void setInformationomrehabilitering(Informationomrehabilitering informationomrehabilitering) {
        this.informationomrehabilitering = informationomrehabilitering;
    }

    @JsonProperty("diagnoskod")
    public List<Kod> getDiagnoskod() {
        return diagnoskod;
    }

    @JsonProperty("diagnoskod")
    public void setDiagnoskod(List<Kod> diagnoskod) {
        this.diagnoskod = diagnoskod;
    }

    @JsonProperty("aktivitetsbegransning")
    public Aktivitetsbegransning getAktivitetsbegransning() {
        return aktivitetsbegransning;
    }

    @JsonProperty("aktivitetsbegransning")
    public void setAktivitetsbegransning(Aktivitetsbegransning aktivitetsbegransning) {
        this.aktivitetsbegransning = aktivitetsbegransning;
    }

    @JsonProperty("funktionsnedsattning")
    public Funktionsnedsattning getFunktionsnedsattning() {
        return funktionsnedsattning;
    }

    @JsonProperty("funktionsnedsattning")
    public void setFunktionsnedsattning(Funktionsnedsattning funktionsnedsattning) {
        this.funktionsnedsattning = funktionsnedsattning;
    }

    @JsonProperty("referens")
    public List<Referen> getReferens() {
        return referens;
    }

    @JsonProperty("referens")
    public void setReferens(List<Referen> referens) {
        this.referens = referens;
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
