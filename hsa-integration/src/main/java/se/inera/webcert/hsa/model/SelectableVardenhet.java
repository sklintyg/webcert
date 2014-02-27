package se.inera.webcert.hsa.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface SelectableVardenhet {

    public String getId();

    public String getNamn();

    @JsonIgnore
    public abstract List<String> getHsaIds();
    
}
