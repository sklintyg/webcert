/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"namn", "id", "fragaSvar", "intyg" })
public class VardenhetStats {

    @JsonProperty("fragaSvar")
    private long ohanteradeFragaSvar;

    @JsonProperty("intyg")
    private long osigneradeIntyg;

    @JsonProperty("namn")
    private String namn;

    @JsonProperty("id")
    private String hsaId;

    public VardenhetStats(@JsonProperty("namn") String namn, @JsonProperty("id") String hsaId) {
        super();
        this.namn = namn;
        this.hsaId = hsaId;
    }

    public long getOhanteradeFragaSvar() {
        return ohanteradeFragaSvar;
    }

    public void setOhanteradeFragaSvar(long ohanteradeFragaSvar) {
        this.ohanteradeFragaSvar = ohanteradeFragaSvar;
    }

    public long getOsigneradeIntyg() {
        return osigneradeIntyg;
    }

    public void setOsigneradeIntyg(long osigneradeIntyg) {
        this.osigneradeIntyg = osigneradeIntyg;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    @Override
    @JsonIgnore
    public String toString() {
        return "VardenhetStats [ohanteradeFragaSvar=" + ohanteradeFragaSvar + ", osigneradeIntyg=" + osigneradeIntyg
                + ", namn=" + namn + ", hsaId=" + hsaId + "]";
    }

}
