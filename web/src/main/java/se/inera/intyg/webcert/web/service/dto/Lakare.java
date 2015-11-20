package se.inera.intyg.webcert.web.service.dto;

public class Lakare {

    private String hsaId;

    private String name;

    public Lakare(String hsaId, String name) {
        this.hsaId = hsaId;
        this.name = name;
    }

    public String getHsaId() {
        return hsaId;
    }

    public String getName() {
        return name;
    }

}
