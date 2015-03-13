package se.inera.webcert.hsa.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface SelectableVardenhet {

    String getId();

    String getNamn();

    @JsonIgnore
    List<String> getHsaIds();
}
