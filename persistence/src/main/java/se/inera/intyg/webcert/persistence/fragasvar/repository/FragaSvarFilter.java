package se.inera.intyg.webcert.persistence.fragasvar.repository;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 * Created by pehr on 10/21/13.
 */
public class FragaSvarFilter {

    private List<String> enhetsIds = new ArrayList<String>();

    private boolean questionFromFK;
    private boolean questionFromWC;

    private String hsaId;

    private Boolean vidarebefordrad;

    private LocalDateTime changedFrom;
    private LocalDateTime changedTo;

    private VantarPa vantarPa = VantarPa.ALLA_OHANTERADE;

    private LocalDate replyLatest;

    private Integer startFrom;

    private Integer pageSize;

    public boolean hasPageSizeAndStartFrom() {
        return (pageSize != null && startFrom != null);
    }

    public List<String> getEnhetsIds() {
        return enhetsIds;
    }

    public void setEnhetsIds(List<String> enhetsIds) {
        this.enhetsIds = enhetsIds;
    }

    public boolean isQuestionFromFK() {
        return questionFromFK;
    }

    public void setQuestionFromFK(boolean questionFromFK) {
        this.questionFromFK = questionFromFK;
    }

    public boolean isQuestionFromWC() {
        return questionFromWC;
    }

    public void setQuestionFromWC(boolean questionFromWC) {
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

    public LocalDate getReplyLatest() {
        return replyLatest;
    }

    public void setReplyLatest(LocalDate replyLatest) {
        this.replyLatest = replyLatest;
    }

    public VantarPa getVantarPa() {
        return vantarPa;
    }

    public void setVantarPa(VantarPa vantarPa) {
        this.vantarPa = vantarPa;
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
