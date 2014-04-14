package se.inera.webcert.hsa.model;

public abstract class AbstractVardenhet implements SelectableVardenhet {

    private String id;

    private String namn;

    public AbstractVardenhet() {
        super();
    }

    public AbstractVardenhet(String id, String namn) {
        super();
        this.id = id;
        this.namn = namn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }
}
