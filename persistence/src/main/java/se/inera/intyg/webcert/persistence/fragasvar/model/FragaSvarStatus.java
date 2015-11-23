package se.inera.intyg.webcert.persistence.fragasvar.model;

public class FragaSvarStatus {

    private Long fragaSvarId;

    private String frageStallare;

    private String svarsText;

    private Status status;

    public FragaSvarStatus(Long fragaSvarId, String frageStallare, String svarsText, Status status) {
        super();
        this.fragaSvarId = fragaSvarId;
        this.frageStallare = frageStallare;
        this.svarsText = svarsText;
        this.status = status;
    }

    public Long getFragaSvarId() {
        return fragaSvarId;
    }

    public String getFrageStallare() {
        return frageStallare;
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

    public boolean isClosed() {
        return (status.equals(Status.CLOSED));
    }

}
