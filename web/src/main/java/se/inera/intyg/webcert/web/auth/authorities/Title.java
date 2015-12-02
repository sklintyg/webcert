package se.inera.intyg.webcert.web.auth.authorities;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Magnus Ekstrand on 19/11/15.
 */
public class Title {

    @JsonProperty
    private String title;

    @JsonProperty
    private String desc;

    @JsonProperty
    private Role role;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "\nTitle {"
                + " title='" + title + '\''
                + ", desc='" + desc + '\''
                + ", role=" + role
                + "}";
    }

}
