package se.inera.webcert.hsa.model;

import java.io.Serializable;

public abstract class AbstractVardenhet implements SelectableVardenhet, Serializable {

    private static final long serialVersionUID = 5313273402235906664L;

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
