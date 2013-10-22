package se.inera.webcert.persistence.fragasvar.model;

import org.joda.time.LocalDate;

/**
 * Created by pehr on 10/21/13.
 */
public class FragaSvarFilter {

    private boolean questionFromFK;
    private boolean questionFromWC;

    private String hsaId;

    private boolean  statusClosed;
    private boolean statusOpen;

    //TODO do we need a "NotVidarebefordrad". How do we show all?
    private boolean vidarebefordrad;

    private LocalDate changedFrom;
    private LocalDate changedTo;

    //TODO is subject always 1 or All. Can user select multiple?
    private Amne subject;

    private LocalDate replyLatest;

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

    public boolean isStatusClosed() {
        return statusClosed;
    }

    public void setStatusClosed(boolean statusClosed) {
        this.statusClosed = statusClosed;
    }

    public boolean isStatusOpen() {
        return statusOpen;
    }

    public void setStatusOpen(boolean statusOpen) {
        this.statusOpen = statusOpen;
    }

    public boolean isVidarebefordrad() {
        return vidarebefordrad;
    }

    public void setVidarebefordrad(boolean vidarebefordrad) {
        this.vidarebefordrad = vidarebefordrad;
    }

    public LocalDate getChangedFrom() {
        return changedFrom;
    }

    public void setChangedFrom(LocalDate changedFrom) {
        this.changedFrom = changedFrom;
    }

    public LocalDate getChangedTo() {
        return changedTo;
    }

    public void setChangedTo(LocalDate changedTo) {
        this.changedTo = changedTo;
    }

    public Amne getSubject() {
        return subject;
    }

    public void setSubject(Amne subject) {
        this.subject = subject;
    }

    public LocalDate getReplyLatest() {
        return replyLatest;
    }

    public void setReplyLatest(LocalDate replyLatest) {
        this.replyLatest = replyLatest;
    }
}
