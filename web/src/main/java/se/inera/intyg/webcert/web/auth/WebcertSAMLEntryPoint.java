/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.websso.WebSSOProfileOptions;

import se.inera.intyg.webcert.web.auth.common.AuthConstants;

/**
 * Custom SAMLEntryPoint for Webcert that overrides the generation of AuthnContexts based on metadata alias:
 *
 * For SITHS (defaultAlias), we only supply the {@link AuthConstants#URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT}
 *
 * For ELEG (eleg), we do not specify anything. This is due to SSO problems at CGIs end when a previously authenticated
 * identity is validated against the IdP to access another system in the same federation.
 *
 * Created by eriklupander on 2015-11-24.
 */
public class WebcertSAMLEntryPoint extends SAMLEntryPoint {

    /**
     * Override from superclass, see class comment for details.
     *
     * @param context
     *            containing local entity
     * @param exception
     *            exception causing invocation of this entry point (can be null)
     * @return populated webSSOprofile
     * @throws MetadataProviderException
     *             in case metadata loading fails
     */
    @Override
    protected WebSSOProfileOptions getProfileOptions(SAMLMessageContext context, AuthenticationException exception)
            throws MetadataProviderException {

        WebSSOProfileOptions ssoProfileOptions;
        if (defaultOptions != null) {
            ssoProfileOptions = defaultOptions.clone();

            if (context.getLocalExtendedMetadata().getAlias().equals(AuthConstants.ALIAS_ELEG)) {
                ssoProfileOptions.setAuthnContexts(new HashSet<>());
            } else if (context.getLocalExtendedMetadata().getAlias().equals(AuthConstants.ALIAS_SITHS)) {
                ssoProfileOptions.setAuthnContexts(buildTlsClientAuthContexts());
            }
        } else {
            ssoProfileOptions = new WebSSOProfileOptions();
        }

        return ssoProfileOptions;
    }

    private Collection<String> buildTlsClientAuthContexts() {
        Set<String> set = new HashSet<>();
        set.add(AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT);
        return set;
    }
}
