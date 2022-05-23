/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.list.dto;

public class PatientListInfo {
    private String id;
    private boolean protectedPerson;
    private boolean deceased;
    private boolean testIndicated;

    public PatientListInfo(String id, boolean protectedPerson, boolean deceased, boolean testIndicated) {
        this.id = id;
        this.protectedPerson = protectedPerson;
        this.deceased = deceased;
        this.testIndicated = testIndicated;
    }

    public boolean isProtectedPerson() {
        return protectedPerson;
    }

    public void setProtectedPerson(boolean protectedPerson) {
        this.protectedPerson = protectedPerson;
    }

    public boolean isDeceased() {
        return deceased;
    }

    public void setDeceased(boolean deceased) {
        this.deceased = deceased;
    }

    public boolean isTestIndicated() {
        return testIndicated;
    }

    public void setTestIndicated(boolean testIndicated) {
        this.testIndicated = testIndicated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
