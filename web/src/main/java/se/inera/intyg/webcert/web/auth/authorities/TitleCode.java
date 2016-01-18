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
