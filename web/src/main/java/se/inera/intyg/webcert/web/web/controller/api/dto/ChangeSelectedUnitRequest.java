package se.inera.intyg.webcert.web.web.controller.api.dto;

public class ChangeSelectedUnitRequest {

    private String id;

    private String namn;

    public ChangeSelectedUnitRequest() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

}
