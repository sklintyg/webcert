package se.inera.intyg.webcert.web.auth.authorities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

/**
 * Created by Magnus Ekstrand on 19/11/15.
 */
public class Privilege {

    @JsonProperty
    private String name;

    @JsonProperty
    private String desc;

    @JsonProperty
    private List<String> intygstyper;

    @JsonProperty
    private List<String> requestOrigins;


    // ~ Getter and setter
    // =======================================================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<String> getIntygstyper() {
        return intygstyper;
    }

    public void setIntygstyper(List<String> intygstyper) {
        this.intygstyper = intygstyper;
    }

    public List<String> getRequestOrigins() {
        return requestOrigins;
    }


    public void setRequestOrigins(List<String> requestOrigins) {
        this.requestOrigins = requestOrigins;
    }

    // ~ API
    // =======================================================================

    @Override
    public String toString() {
        return "\nPrivilege {"
                + " name='" + name + '\''
                + ", desc='" + desc + '\''
                + ", intygstyper= " + intygstyper
                + ", requestOrigins= " + requestOrigins
                + "}";
    }

}
