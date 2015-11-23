package se.inera.intyg.webcert.web.web.controller.api.dto;

public class CopyIntygResponse {

    private String intygsUtkastId;

    private String intygsTyp;

    public CopyIntygResponse(String intygsUtkastId, String intygsTyp) {
        super();
        this.intygsUtkastId = intygsUtkastId;
        this.intygsTyp = intygsTyp;
    }

    public String getIntygsUtkastId() {
        return intygsUtkastId;
    }

    public void setIntygsUtkastId(String intygsUtkastId) {
        this.intygsUtkastId = intygsUtkastId;
    }

    public String getIntygsTyp() {
        return intygsTyp;
    }

    public void setIntygsTyp(String intygsTyp) {
        this.intygsTyp = intygsTyp;
    }
}
