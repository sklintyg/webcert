package se.inera.intyg.webcert.web.service.facade.list.dto;

import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;

import java.time.LocalDateTime;

public class ListDraftFilter {
    private boolean forwarded;
    private DraftStatus status;
    private LocalDateTime savedFrom;
    private LocalDateTime savedTo;
    private String savedByHsaID;
    private String patientId;
    private Integer pageSize;
    private Integer startFrom;
    private ListColumnType orderBy;
    private boolean ascending;

    public ListDraftFilter() {}

    public ListDraftFilter(boolean forwarded, DraftStatus status, LocalDateTime savedFrom, LocalDateTime savedTo, String savedByHsaID, String patientId, int pageSize, Integer startFrom, ListColumnType orderBy, boolean ascending) {
        this.forwarded = forwarded;
        this.status = status;
        this.savedFrom = savedFrom;
        this.savedTo = savedTo;
        this.savedByHsaID = savedByHsaID;
        this.patientId = patientId;
        this.pageSize = pageSize;
        this.startFrom = startFrom;
        this.orderBy = orderBy;
        this.ascending = ascending;
    }


    public boolean isForwarded() {
        return forwarded;
    }

    public void setForwarded(boolean forwarded) {
        this.forwarded = forwarded;
    }

    public DraftStatus getStatus() {
        return status;
    }

    public void setStatus(DraftStatus status) {
        this.status = status;
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

    public String getSavedByHsaID() {
        return savedByHsaID;
    }

    public void setSavedByHsaID(String savedByHsaID) {
        this.savedByHsaID = savedByHsaID;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(Integer startFrom) {
        this.startFrom = startFrom;
    }

    public ListColumnType getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(ListColumnType orderBy) {
        this.orderBy = orderBy;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }
}
