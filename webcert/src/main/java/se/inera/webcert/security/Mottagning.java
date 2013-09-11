package se.inera.webcert.security;

/**
 * @author andreaskaltenbach
 */
public class Mottagning {

    private String namn;
    private String id;

    public Mottagning() {

    }

    public Mottagning(String id, String namn) {
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
}
