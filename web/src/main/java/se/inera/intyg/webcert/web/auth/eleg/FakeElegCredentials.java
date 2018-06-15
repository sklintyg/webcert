/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.auth.eleg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import se.inera.intyg.webcert.web.auth.common.FakeCredential;

/**
 * Fake container for approx. CGI SAML ticket attributes.
 *
 * Created by eriklupander on 2015-06-16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FakeElegCredentials implements FakeCredential {

    // Subject_SerialNumber
    private String personId;

    // Subject_GivenName
    private String firstName;

    // Subject_Surname
    private String lastName;

    private boolean privatLakare;

    private boolean sekretessMarkerad;

    private String authenticationMethod;

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isPrivatLakare() {
        return privatLakare;
    }

    public void setPrivatLakare(boolean privatLakare) {
        this.privatLakare = privatLakare;
    }

    public boolean isSekretessMarkerad() {
        return sekretessMarkerad;
    }

    public void setSekretessMarkerad(boolean sekretessMarkerad) {
        this.sekretessMarkerad = sekretessMarkerad;
    }

    @Override
    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }
}
