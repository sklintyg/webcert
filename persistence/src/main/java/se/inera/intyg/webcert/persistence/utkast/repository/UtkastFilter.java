package se.inera.intyg.webcert.persistence.utkast.repository;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;

public class UtkastFilter {

    private String unitHsaId;

    private String savedByHsaId;

    private Boolean notified;

    private LocalDateTime savedFrom;

    private LocalDateTime savedTo;

    private List<UtkastStatus> statusList = new ArrayList<>();

    private Integer startFrom;

    private Integer pageSize;

    public UtkastFilter(String unitHsaId) {
        this.unitHsaId = unitHsaId;
    }

    public boolean hasPageSizeAndStartFrom() {
        return (pageSize != null && startFrom != null);
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

    public Boolean getNotified() {
        return notified;
    }

    public void setNotified(Boolean notified) {
        this.notified = notified;
    }

    public LocalDateTime getSavedFrom() {
        return savedFrom;
    }

    public void setSavedFrom(LocalDateTime savedFrom) {
        this.savedFrom = savedFrom;
    }

    public LocalDateTime getSavedTo() {
        return savedTo;
    }

    public void setSavedTo(LocalDateTime savedTo) {
        this.savedTo = savedTo;
    }

    public List<UtkastStatus> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<UtkastStatus> statusList) {
        this.statusList = statusList;
    }

    public Integer getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(Integer startFrom) {
        this.startFrom = startFrom;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

}
