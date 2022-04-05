package se.inera.intyg.webcert.web.service.facade.list.config.dto;

public class StaffListInfo {
    private String hsaId;
    private String name;

    public StaffListInfo(String hsaId, String name) {
        this.hsaId = hsaId;
        this.name = name;
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
