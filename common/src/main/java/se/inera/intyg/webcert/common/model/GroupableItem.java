package se.inera.intyg.webcert.common.model;

/**
 * Created by eriklupander on 2017-09-14.
 */
public class GroupableItem {

    private String id;
    private String enhetsId;
    private String personnummer;
    private String intygsTyp;
    private SekretessStatus sekretessStatus;

    public GroupableItem(Long id, String enhetsId, String personnummer, String intygsTyp) {
        this.id = Long.toString(id);
        this.enhetsId = enhetsId;
        this.personnummer = personnummer;
        this.intygsTyp = intygsTyp;
    }

    public GroupableItem(String id, String enhetsId, String personnummer, String intygsTyp) {
        this.id = id;
        this.enhetsId = enhetsId;
        this.personnummer = personnummer;
        this.intygsTyp = intygsTyp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnhetsId() {
        return enhetsId;
    }

    public void setEnhetsId(String enhetsId) {
        this.enhetsId = enhetsId;
    }

    public String getPersonnummer() {
        return personnummer;
    }

    public void setPersonnummer(String personnummer) {
        this.personnummer = personnummer;
    }

    public String getIntygsTyp() {
        return intygsTyp;
    }

    public void setIntygsTyp(String intygsTyp) {
        this.intygsTyp = intygsTyp;
    }

    public SekretessStatus getSekretessStatus() {
        return sekretessStatus;
    }

    public void setSekretessStatus(SekretessStatus sekretessStatus) {
        this.sekretessStatus = sekretessStatus;
    }
}
