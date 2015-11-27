package se.inera.intyg.webcert.web.web.controller.api.dto;

/**
 * Created by stephenwhite on 18/02/15.
 */
public class QARequest {
    private String intygsTyp;
    private Long fragaSvarId;

    public QARequest() {
    }

    public String getIntygsTyp() {
        return intygsTyp;
    }

    public void setIntygsTyp(String intygsTyp) {
        this.intygsTyp = intygsTyp;
    }

    public Long getFragaSvarId() {
        return fragaSvarId;
    }

    public void setFragaSvarId(Long fragaSvarId) {
        this.fragaSvarId = fragaSvarId;
    }
}
