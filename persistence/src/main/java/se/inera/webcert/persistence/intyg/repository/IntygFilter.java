package se.inera.webcert.persistence.intyg.repository;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

import se.inera.webcert.persistence.intyg.model.IntygsStatus;

public class IntygFilter {

    private String unitHsaId;

    private String savedByHsaId;

    private Boolean vidarebefordrad;

    private LocalDateTime changedFrom;
    private LocalDateTime changedTo;

    private List<IntygsStatus> statusList = new ArrayList<>();

    public IntygFilter(String unitHsaId) {
        this.unitHsaId = unitHsaId;
    }
    
    public String getUnitHsaId() {
        return unitHsaId;
    }

    public void setUnitHsaId(String unitHsaId) {
        this.unitHsaId = unitHsaId;
    }

    public String getSavedByHsaId() {
        return savedByHsaId;
    }

    public void setSavedByHsaId(String savedByHsaId) {
        this.savedByHsaId = savedByHsaId;
    }

    public Boolean getVidarebefordrad() {
        return vidarebefordrad;
    }

    public void setVidarebefordrad(Boolean vidarebefordrad) {
        this.vidarebefordrad = vidarebefordrad;
    }

    public LocalDateTime getChangedFrom() {
        return changedFrom;
    }

    public void setChangedFrom(LocalDateTime changedFrom) {
        this.changedFrom = changedFrom;
    }

    public LocalDateTime getChangedTo() {
        return changedTo;
    }

    public void setChangedTo(LocalDateTime changedTo) {
        this.changedTo = changedTo;
    }

    public List<IntygsStatus> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<IntygsStatus> statusList) {
        this.statusList = statusList;
    }

}
