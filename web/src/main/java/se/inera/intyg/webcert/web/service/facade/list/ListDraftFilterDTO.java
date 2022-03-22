package se.inera.intyg.webcert.web.service.facade.list;

import java.time.LocalDateTime;

public class ListDraftFilterDTO {
    private boolean forwarded;
    private DraftStatusDTO status;
    private LocalDateTime savedFrom;
    private LocalDateTime savedTo;
    private String savedByHsaID;
    private String patientId;
    private Integer pageSize;
    private Integer startFrom;
    private CertificateListOrderTypeDTO orderBy;
    private boolean ascending;

    public ListDraftFilterDTO() {}

    public ListDraftFilterDTO(boolean forwarded, DraftStatusDTO status, LocalDateTime savedFrom, LocalDateTime savedTo, String savedByHsaID, String patientId, int pageSize, Integer startFrom, CertificateListOrderTypeDTO orderBy, boolean ascending) {
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

    public DraftStatusDTO getStatus() {
        return status;
    }

    public void setStatus(DraftStatusDTO status) {
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

    public CertificateListOrderTypeDTO getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(CertificateListOrderTypeDTO orderBy) {
        this.orderBy = orderBy;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }
}
