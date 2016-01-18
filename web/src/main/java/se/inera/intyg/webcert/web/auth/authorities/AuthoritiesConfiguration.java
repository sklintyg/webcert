/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    private List<RequestOrigin> requestOrigins;
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

    public List<RequestOrigin> getRequestOrigins() {
        return requestOrigins;
    }

    public void setRequestOrigins(List<RequestOrigin> requestOrigins) {
        this.requestOrigins = requestOrigins;
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
                .append(format("KnownRequestOrigins: %s\n", knownRequestOrigins))
                .append(format("KnownRoles: %s\n", knownRoles))
                .append(format("KnownPrivileges: %s\n", knownPrivileges))
                .append(format("KnownIntygstyper: %s\n", knownIntygstyper))
                .append(format("RequestOrigins: %s\n", requestOrigins))
                .append(format("Privileges: %s\n", privileges))
                .append(format("Roles: %s\n", roles))
                .append(format("Titles: %s\n", titles))
                .append(format("TitleCodes: %s\n", titleCodes))
                .toString();
    }

}
