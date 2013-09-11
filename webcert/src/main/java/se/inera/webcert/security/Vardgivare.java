package se.inera.webcert.security;

import java.util.ArrayList;
import java.util.List;

/**
 * @author andreaskaltenbach
 */
public class Vardgivare {

    private String namn;
    private String id;

    private List<Vardenhet> vardenheter = new ArrayList<>();

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

    public List<Vardenhet> getVardenheter() {
        return vardenheter;
    }

    public void setVardenheter(List<Vardenhet> vardenheter) {
        this.vardenheter = vardenheter;
    }
}
