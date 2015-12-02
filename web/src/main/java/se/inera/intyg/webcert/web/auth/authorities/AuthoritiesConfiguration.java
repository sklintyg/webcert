package se.inera.intyg.webcert.web.auth.authorities;

import static java.lang.String.format;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

/**
 * Created by Magnus Ekstrand on 18/11/15.
 */
public final class AuthoritiesConfiguration {

    @JsonProperty
    private String version;
    @JsonProperty
    private Date released;
    @JsonProperty
    private String changedby;
    @JsonProperty
    private List<String> knownRequestOrigins;
    @JsonProperty
    private List<String> knownRoles;
    @JsonProperty
    private List<String> knownPrivileges;
    @JsonProperty
    private List<String> knownIntygstyper;
    @JsonProperty
    private List<Privilege> privileges;
    @JsonProperty
    private List<Role> roles;
    @JsonProperty
    private List<Title> titles;
    @JsonProperty
    private List<TitleCode> titleCodes;


    // ~ Getter and setter
    // ==================================================================

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getReleased() {
        return released;
    }

    public void setReleased(Date released) {
        this.released = released;
    }

    public String getChangedby() {
        return changedby;
    }

    public void setChangedby(String changedby) {
        this.changedby = changedby;
    }

    public List<String> getKnownRequestOrigins() {
        return knownRequestOrigins;
    }

    public void setKnownRequestOrigins(List<String> knownRequestOrigins) {
        this.knownRequestOrigins = knownRequestOrigins;
    }

    public List<String> getKnownRoles() {
        return knownRoles;
    }

    public void setKnownRoles(List<String> knownRoles) {
        this.knownRoles = knownRoles;
    }

    public List<String> getKnownPrivileges() {
        return knownPrivileges;
    }

    public void setKnownPrivileges(List<String> knownPrivileges) {
        this.knownPrivileges = knownPrivileges;
    }

    public List<String> getKnownIntygstyper() {
        return knownIntygstyper;
    }

    public void setKnownIntygstyper(List<String> knownIntygstyper) {
        this.knownIntygstyper = knownIntygstyper;
    }

    public List<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<Title> getTitles() {
        return titles;
    }

    public void setTitles(List<Title> titles) {
        this.titles = titles;
    }

    public List<TitleCode> getTitleCodes() {
        return titleCodes;
    }

    public void setTitleCodes(List<TitleCode> titleCodes) {
        this.titleCodes = titleCodes;
    }


    // ~ API
    // ==================================================================

    @Override
    public String toString() {
        return new StringBuilder()
                .append(format("Version: %s\n", version))
                .append(format("Released: %s\n", released))
                .append(format("Changedby: %s\n", changedby))
                .append(format("KnownRoles: %s\n", knownRoles))
                .append(format("KnownPrivileges: %s\n", knownPrivileges))
                .append(format("KnownIntygstyper: %s\n", knownIntygstyper))
                .append(format("Privileges: %s\n", privileges))
                .append(format("Roles: %s\n", roles))
                .append(format("Titles: %s\n", titles))
                .append(format("TitleCodes: %s\n", titleCodes))
                .toString();
    }

}
