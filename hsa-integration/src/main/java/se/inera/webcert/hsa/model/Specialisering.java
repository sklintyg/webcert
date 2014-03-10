package se.inera.webcert.hsa.model;

public class Specialisering {

    private String kod;
    
    private String namn;
    
    public Specialisering() {
        super();
    }

    public Specialisering(String kod, String namn) {
        super();
        this.kod = kod;
        this.namn = namn;
    }

    public String getKod() {
        return kod;
    }
    
    public void setKod(String kod) {
        this.kod = kod;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }
    
}
