package se.inera.intyg.webcert.common.model;

/**
 * This class is a general-purpose class for allowing JPQL queries to construct objects meant to be groupable using
 * the Java 8 streams API.
 *
 * The 'id' should always be unique.
 * The 'enhetsId' is typically used as group by expression.
 * The 'personnummer' and 'intygsTyp' is typically used for performing user/patient context filtering for sekretessmarkering.
 * The 'sekretessstatus' is never populated when constructing the GroupableItem as we must query the PU-service for this
 * information.
 *
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
