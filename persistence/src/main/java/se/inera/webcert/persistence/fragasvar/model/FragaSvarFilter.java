package se.inera.webcert.persistence.fragasvar.model;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 * Created by pehr on 10/21/13.
 */
public class FragaSvarFilter {

    private boolean questionFromFK;
    private boolean questionFromWC;

    private String hsaId;

    //TODO do we need a "NotVidarebefordrad". How do we show all?
    private boolean vidarebefordrad;

    private LocalDateTime changedFrom;
    private LocalDate changedTo;

    private Status status;

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

    public boolean isVidarebefordrad() {
        return vidarebefordrad;
    }

    public void setVidarebefordrad(boolean vidarebefordrad) {
        this.vidarebefordrad = vidarebefordrad;
    }

    public LocalDateTime getChangedFrom() {
        return changedFrom;
    }

    public void setChangedFrom(LocalDateTime changedFrom) {
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
