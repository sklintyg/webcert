package se.inera.webcert.persistence.fragasvar.repository;

/**
 * Created by pehr on 31/10/13.
 */
public class LakarIdNamn {
    protected String hsaId;
    protected String name;

    public LakarIdNamn(String hsaId, String name) {
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
