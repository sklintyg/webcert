package se.inera.intyg.webcert.web.auth.authorities;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Magnus Ekstrand on 19/11/15.
 */
public class TitleCode {

    @JsonProperty
    private String titleCode;
    @JsonProperty
    private String groupPrescriptionCode;
    @JsonProperty
    private Role role;


    public String getTitleCode() {
        return titleCode;
    }

    public void setTitleCode(String titleCode) {
        this.titleCode = titleCode;
    }

    public String getGroupPrescriptionCode() {
        return groupPrescriptionCode;
    }

    public void setGroupPrescriptionCode(String groupPrescriptionCode) {
        this.groupPrescriptionCode = groupPrescriptionCode;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }


    @Override
    public String toString() {
        return "\nTitleCode {"
                + " titleCode='" + titleCode + '\''
                + ", groupPrescriptionCode='" + groupPrescriptionCode + '\''
                + ", role=" + role
                + "}";
    }
}
