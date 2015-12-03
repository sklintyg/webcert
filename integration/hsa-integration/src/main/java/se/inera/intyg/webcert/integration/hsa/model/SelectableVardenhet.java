package se.inera.intyg.webcert.integration.hsa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface SelectableVardenhet {

    String getId();

    String getNamn();

    @JsonIgnore
    List<String> getHsaIds();
}
