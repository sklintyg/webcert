package se.inera.intyg.webcert.logmessages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class Enhet implements Serializable {

    private static final long serialVersionUID = 1L;
    private String enhetsId;
    private String enhetsNamn;

    private String vardgivareId;
    private String vardgivareNamn;

    public Enhet() {
    }

    public Enhet(String enhetsId, String vardgivareId) {
        this(enhetsId, null, vardgivareId, null);
    }

    public Enhet(String enhetsId, String enhetsNamn, String vardgivareId, String vardgivareNamn) {
        this.enhetsId = enhetsId;
        this.enhetsNamn = enhetsNamn;
        this.vardgivareId = vardgivareId;
        this.vardgivareNamn = vardgivareNamn;
    }

    public String getEnhetsId() {
        return enhetsId;
    }

    public String getEnhetsNamn() {
        return enhetsNamn;
    }

    public String getVardgivareId() {
        return vardgivareId;
    }

    public String getVardgivareNamn() {
        return vardgivareNamn;
    }
}
