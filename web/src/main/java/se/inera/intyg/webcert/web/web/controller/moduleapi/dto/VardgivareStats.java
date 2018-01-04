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
package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"namn", "id", "vardenheter" })
public class VardgivareStats {

    @JsonProperty("namn")
    private String namn;

    @JsonProperty("id")
    private String hsaId;

    private List<VardenhetStats> vardenheter = new ArrayList<>();

    public VardgivareStats(@JsonProperty("namn") String namn, @JsonProperty("id") String hsaId) {
        super();
        this.namn = namn;
        this.hsaId = hsaId;
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

    public List<VardenhetStats> getVardenheter() {
        return vardenheter;
    }

    public void setVardenheter(List<VardenhetStats> vardenheter) {
        this.vardenheter = vardenheter;
    }

    @Override
    @JsonIgnore
    public String toString() {
        return "VardgivareStats [namn=" + namn + ", hsaId=" + hsaId + ", vardenheter=" + vardenheter + "]";
    }

}
