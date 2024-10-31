/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.auth.common;

import static se.inera.intyg.webcert.web.auth.common.AuthConstants.ELEG_AUTHN_CLASSES;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SITHS_AUTHN_CLASSES;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_UNSPECIFIED;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.auth.eleg.ElegWebCertUserDetailsService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * Created by eriklupander on 2015-08-12.
 *
 * Facade for our two different UserDetailsServices that looks up authenticated logins over either SITHS or e-leg.
 *
 * Checks the Authentication context class ref to determine method:
 *
 * <li>http://id.sambi.se/loa/loa3 - SITHS</li>
 * <li>urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI - E-leg</li>
 * <li>urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI - E-leg</li>
 * <li>urn:oasis:names:tc:SAML:2.0:ac:classes:MobileTwofactorContract - E-leg</li>
 *
 * For testing purposes, this class is also aware of urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified, but will only
 * initiate authorization if the application has the "dev" spring profile active.
 */
@Service
@RequiredArgsConstructor
public class UnifiedUserDetailsService {

    private final Environment environment;
    private final ElegWebCertUserDetailsService elegWebCertUserDetailsService;
    private final WebcertUserDetailsService webcertUserDetailsService;

    public WebCertUser buildUserPrincipal(String userId, String authenticationScheme) {
        if (authenticationScheme == null) {
            throw new IllegalArgumentException("Cannot determine which underlying UserDetailsService to use for SAMLCredential. "
                + "Must contain an authenticationAssertion");
        }

        if (ELEG_AUTHN_CLASSES.contains(authenticationScheme)) {
            return elegWebCertUserDetailsService.buildUserPrincipal(userId, authenticationScheme);

        } else if (SITHS_AUTHN_CLASSES.contains(authenticationScheme)) {
            return webcertUserDetailsService.buildUserPrincipal(userId, authenticationScheme);

        } else if (URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_UNSPECIFIED.equals(authenticationScheme)) {
            if (Arrays.stream(environment.getActiveProfiles()).anyMatch("wc-security-test"::equalsIgnoreCase)) {
                return webcertUserDetailsService.buildUserPrincipal(userId, authenticationScheme);
            }
            throw new IllegalArgumentException(
                "Authentication scheme " + URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_UNSPECIFIED + " is not allowed");

        } else {
            throw new IllegalArgumentException("Authentication scheme was " + authenticationScheme + ", expected one of: "
                + SITHS_AUTHN_CLASSES + " or " + ELEG_AUTHN_CLASSES);
        }
    }
}
