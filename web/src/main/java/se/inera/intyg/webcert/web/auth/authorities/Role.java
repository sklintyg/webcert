package se.inera.intyg.webcert.web.auth.authorities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by mango on 19/11/15.
 */
public class Role {

    @JsonProperty
    private String name;

    @JsonProperty
    private String desc;

    @JsonProperty
    private List<Privilege> privileges;


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

    public List<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
    }


    @Override
    public String toString() {
        return "\nRole {"
                + " name='" + name + '\''
                + ", desc='" + desc + '\''
                + ", privileges " + privileges
                + "}";
    }

}
