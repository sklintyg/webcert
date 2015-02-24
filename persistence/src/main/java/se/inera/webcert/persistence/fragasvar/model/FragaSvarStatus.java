package se.inera.webcert.persistence.fragasvar.model;

public class FragaSvarStatus {

    private Long fragaSvarId;

    private String svarsText;

    private Status status;

    public FragaSvarStatus(Long fragaSvarId, String svarsText, Status status) {
        super();
        this.fragaSvarId = fragaSvarId;
        this.svarsText = svarsText;
        this.status = status;
    }

    public Long getFragaSvarId() {
        return fragaSvarId;
    }

    public String getSvarsText() {
        return svarsText;
    }

    public Status getStatus() {
        return status;
    }

    public boolean hasAnswerSet() {
        return (svarsText != null);
    }

}
