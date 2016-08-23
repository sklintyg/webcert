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
package se.inera.intyg.webcert.web.auth;

import org.opensaml.saml2.core.AuthnStatement;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLCredential;
import se.inera.intyg.webcert.web.auth.common.AuthConstants;

import java.util.Date;

/**
 * This customized AuthenticationProvider provides custom logic for setting the expiration date of the SAML
 * credential.
 *
 * For SITHS-based authentications we typically get 60 minutes from the IdP, but we can safely ignore this by setting
 * the expiration to null which mitigates the problem of users being automatically logged out after 60 minutes regardless
 * of user activity.
 *
 * Created by eriklupander on 2016-08-18.
 */
public class WebcertAuthenticationProvider extends SAMLAuthenticationProvider {

    /**
     * Retrieves the expirationDate per the SAML credential using the superclass method, but then overrides its value with null
     * if there is a "SITHS" authenticationStatement.
     *
     * @param credential credential to use for expiration parsing.
     * @return null if no expiration is present OR if this is a SITHS-based authentication. Otherwise, expiration time
     *          based on SAML attribute onOrAfter.
     */
    @Override
    protected Date getExpirationDate(SAMLCredential credential) {
        Date expirationDate = super.getExpirationDate(credential);
        for (AuthnStatement statement : credential.getAuthenticationAssertion().getAuthnStatements()) {
            if (isSithsAuthentication(statement)) {
                return null;
            }
        }
        return expirationDate;
    }

    private boolean isSithsAuthentication(AuthnStatement statement) {
        if (statement == null) {
            return false;
        }
        if (statement.getAuthnContext() == null) {
            return false;
        }
        if (statement.getAuthnContext().getAuthnContextClassRef() == null) {
            return false;
        }
        if (statement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef() == null) {
            return false;
        }
        return statement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef().equals(AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT);
    }
}
