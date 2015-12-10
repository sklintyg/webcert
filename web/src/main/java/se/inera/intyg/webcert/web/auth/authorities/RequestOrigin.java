package se.inera.intyg.webcert.web.auth.authorities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * Created by Magnus Ekstrand on 19/11/15.
 */
public class RequestOrigin {

    @JsonProperty
    private String name;

    @JsonProperty
    private List<String> intygstyper;


    // ~ Getter and setter
    // =======================================================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getIntygstyper() {
        return intygstyper;
    }

    public void setIntygstyper(List<String> intygstyper) {
        if (intygstyper == null) {
            this.intygstyper = Collections.emptyList();
        } else {
            this.intygstyper = intygstyper;
        }
    }


    // ~ API
    // =======================================================================

    @Override
    public String toString() {
        return "\nPrivilege {"
                + " name='" + name + '\''
                + ", intygstyper= " + intygstyper
                + "}";
    }

}
