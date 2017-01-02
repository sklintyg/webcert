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
package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"fragaSvarValdEnhet", "fragaSvarAndraEnheter", "intygAndraEnheter", "intygValdEnhet", "vardgivare" })
public class StatsResponse {

    @JsonProperty("fragaSvarAndraEnheter")
    private long totalNbrOfUnhandledFragaSvarOnOtherThanSelected = 0;

    @JsonProperty("fragaSvarValdEnhet")
    private long totalNbrOfUnhandledFragaSvarOnSelected = 0;

    @JsonProperty("intygAndraEnheter")
    private long totalNbrOfUnsignedDraftsOnOtherThanSelected = 0;

    @JsonProperty("intygValdEnhet")
    private long totalNbrOfUnsignedDraftsOnSelected = 0;

    @JsonProperty("vardgivare")
    private List<VardgivareStats> vardgivare = new ArrayList<>();

    public long getTotalNbrOfUnhandledFragaSvarOnOtherThanSelected() {
        return totalNbrOfUnhandledFragaSvarOnOtherThanSelected;
    }

    public void setTotalNbrOfUnhandledFragaSvarOnOtherThanSelected(long totalNbrOfUnhandledFragaSvarOnOtherThanSelected) {
        this.totalNbrOfUnhandledFragaSvarOnOtherThanSelected = totalNbrOfUnhandledFragaSvarOnOtherThanSelected;
    }

    public long getTotalNbrOfUnhandledFragaSvarOnSelected() {
        return totalNbrOfUnhandledFragaSvarOnSelected;
    }

    public void setTotalNbrOfUnhandledFragaSvarOnSelected(long totalNbrOfUnhandledFragaSvarOnSelected) {
        this.totalNbrOfUnhandledFragaSvarOnSelected = totalNbrOfUnhandledFragaSvarOnSelected;
    }

    public long getTotalNbrOfUnsignedDraftsOnOtherThanSelected() {
        return totalNbrOfUnsignedDraftsOnOtherThanSelected;
    }

    public void setTotalNbrOfUnsignedDraftsOnOtherThanSelected(long totalNbrOfUnsignedDraftsOnOtherThanSelected) {
        this.totalNbrOfUnsignedDraftsOnOtherThanSelected = totalNbrOfUnsignedDraftsOnOtherThanSelected;
    }

    public long getTotalNbrOfUnsignedDraftsOnSelected() {
        return totalNbrOfUnsignedDraftsOnSelected;
    }

    public void setTotalNbrOfUnsignedDraftsOnSelected(long totalNbrOfUnsignedDraftsOnSelected) {
        this.totalNbrOfUnsignedDraftsOnSelected = totalNbrOfUnsignedDraftsOnSelected;
    }

    public List<VardgivareStats> getVardgivare() {
        return vardgivare;
    }

    public void setVardgivare(List<VardgivareStats> vardgivare) {
        this.vardgivare = vardgivare;
    }

    @Override
    public String toString() {
        return "StatsResponse [totalNbrOfUnhandledFragaSvarOnOtherThanSelected="
                + totalNbrOfUnhandledFragaSvarOnOtherThanSelected + ", totalNbrOfUnhandledFragaSvarOnSelected="
                + totalNbrOfUnhandledFragaSvarOnSelected + ", totalNbrOfUnsignedDraftsOnOtherThanSelected="
                + totalNbrOfUnsignedDraftsOnOtherThanSelected + ", totalNbrOfUnsignedDraftsOnSelected="
                + totalNbrOfUnsignedDraftsOnSelected + ", vardgivare=" + vardgivare + "]";
    }

}
