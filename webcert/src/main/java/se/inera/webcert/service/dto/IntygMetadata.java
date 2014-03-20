package se.inera.webcert.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;


public class IntygMetadata {
    
    private String id;
    
    private String type;
    
    private String patientId;
    
    private LocalDate fromDate;
        
    private LocalDate tomDate;
    
    private List<IntygStatus> statuses = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getTomDate() {
        return tomDate;
    }

    public void setTomDate(LocalDate tomDate) {
        this.tomDate = tomDate;
    }

    public List<IntygStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<IntygStatus> statuses) {
        this.statuses = statuses;
    }
       
}
