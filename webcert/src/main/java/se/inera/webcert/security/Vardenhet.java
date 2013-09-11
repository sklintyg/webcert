package se.inera.webcert.security;

import java.util.ArrayList;
import java.util.List;

/**
 * @author andreaskaltenbach
 */
public class Vardenhet {

    private String namn;
    private String id;

    private List<Mottagning> mottagningar = new ArrayList<>();

    public Vardenhet() {

    }

    public Vardenhet(String id, String namn) {
        this.id = id;
        this.namn = namn;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Mottagning> getMottagningar() {
        return mottagningar;
    }

    public void setMottagningar(List<Mottagning> mottagningar) {
        this.mottagningar = mottagningar;
    }
}
