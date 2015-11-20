package se.inera.intyg.webcert.web.service.fragasvar.dto;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

public class QueryFragaSvarParameter {

    private String enhetId;

    private Boolean questionFromFK;
    private Boolean questionFromWC;

    private String hsaId;

    private Boolean vidarebefordrad;

    private LocalDateTime changedFrom;
    private LocalDateTime changedTo;

    private String vantarPa;

    private LocalDate replyLatest;

    private Integer startFrom;
    private Integer pageSize;

    public String getEnhetId() {
        return enhetId;
    }

    public void setEnhetId(String enhetId) {
        this.enhetId = enhetId;
    }

    public Boolean getQuestionFromFK() {
        return questionFromFK;
    }

    public void setQuestionFromFK(Boolean questionFromFK) {
        this.questionFromFK = questionFromFK;
    }

    public Boolean getQuestionFromWC() {
        return questionFromWC;
    }

    public void setQuestionFromWC(Boolean questionFromWC) {
        this.questionFromWC = questionFromWC;
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
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

    public String getVantarPa() {
        return vantarPa;
    }

    public void setVantarPa(String vantarPa) {
        this.vantarPa = vantarPa;
    }

    public LocalDate getReplyLatest() {
        return replyLatest;
    }

    public void setReplyLatest(LocalDate replyLatest) {
        this.replyLatest = replyLatest;
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
