package se.inera.webcert.web.controller.moduleapi.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"fragaSvarValdEnhet", "fragaSvarAndraEnheter", "vardgivare"})
public class StatsResponse {
    
    @JsonProperty("fragaSvarAndraEnheter")
    private long totalNbrOfUnhandledFragaSvarOnOtherThanSelected = 0;
    
    @JsonProperty("fragaSvarValdEnhet")
    private long totalNbrOfUnhandledFragaSvarOnSelected = 0;
    
    @JsonProperty("vardgivare")
    private List<VardgivareStats> vardgivare = new ArrayList<VardgivareStats>();
    
    public StatsResponse() {
    
    }

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

    public List<VardgivareStats> getVardgivare() {
        return vardgivare;
    }

    public void setVardgivare(List<VardgivareStats> vardgivare) {
        this.vardgivare = vardgivare;
    }

    @Override
    @JsonIgnore
    public String toString() {
        return "StatsResponse [totalNbrOfUnhandledFragaSvarOnOtherThanSelected="
                + totalNbrOfUnhandledFragaSvarOnOtherThanSelected + ", totalNbrOfUnhandledFragaSvarOnSelected="
                + totalNbrOfUnhandledFragaSvarOnSelected + ", vardgivare=" + vardgivare + "]";
    }
    
}
