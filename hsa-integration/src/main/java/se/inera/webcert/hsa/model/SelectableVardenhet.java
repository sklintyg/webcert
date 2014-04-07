package se.inera.webcert.hsa.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface SelectableVardenhet {

    String getId();

    String getNamn();

    @JsonIgnore
    List<String> getHsaIds();
}
