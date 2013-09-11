package se.inera.webcert.security;

/**
 * @author andreaskaltenbach
 */
public class WebCertUser {

    private String namn;
    private boolean lakare;

    private Vardgivare vardgivare;

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public boolean isLakare() {
        return lakare;
    }

    public void setLakare(boolean lakare) {
        this.lakare = lakare;
    }

    public Vardgivare getVardgivare() {
        return vardgivare;
    }

    public void setVardgivare(Vardgivare vardgivare) {
        this.vardgivare = vardgivare;
    }
}
