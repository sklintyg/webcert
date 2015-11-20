package se.inera.intyg.webcert.web.service.intyg.dto;

public class IntygRecipient {

    /* The recipient's id */
    private String id;

    /* The recipient's name */
    private String name;

    /* The certificate type */
    private String intygsTyp;

    public IntygRecipient(String id, String name, String intygsTyp) {
        this.id = id;
        this.name = name;
        this.intygsTyp = intygsTyp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntygsTyp() {
        return intygsTyp;
    }

    public void setIntygsTyp(String intygsTyp) {
        this.intygsTyp = intygsTyp;
    }
}
