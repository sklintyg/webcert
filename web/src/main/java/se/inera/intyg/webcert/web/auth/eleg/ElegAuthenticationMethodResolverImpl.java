/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;

/**
 * Helper service for determining the login method based on the "LoginMethod" SAML attribute.
 *
 * See {@link se.inera.intyg.webcert.web.auth.eleg.ElegLoginMethod} for known LoginMethod codes.
 *
 * Created by eriklupander on 2015-08-24.
 */
@Service
public class ElegAuthenticationMethodResolverImpl implements ElegAuthenticationMethodResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ElegAuthenticationMethodResolverImpl.class);

    @Autowired(required = false)
    private ElegAuthenticationAttributeHelper elegAuthenticationAttributeHelper;

    @Override
    public AuthenticationMethod resolveAuthenticationMethod(SAMLCredential samlCredential) {
        String loginMethod = elegAuthenticationAttributeHelper.getAttribute(samlCredential, CgiElegAssertion.LOGIN_METHOD);

        if (loginMethod == null || loginMethod.trim().length() == 0) {
            throw new IllegalArgumentException(
                "Cannot process SAML ticket for e-leg. Null or empty LoginMethod attribute on Assertion. "
                    + "Must be one of ccp8,ccp10,ccp11,ccp12 or ccp13");
        }

        return resolveAuthenticationMethod(loginMethod);
    }

    private AuthenticationMethod resolveAuthenticationMethod(String loginMethod) {

        ElegLoginMethod loginMethodEnum;
        try {
            loginMethodEnum = ElegLoginMethod.valueOf(loginMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOG.warn("Cannot resolve AuthenticationMethod from SAML attribute 'LoginMethod': " + loginMethod);
            throw new IllegalArgumentException("Could not parse AuthenticationMethod from SAML attribute 'LoginMethod': " + loginMethod);
        }
        switch (loginMethodEnum) {
            case CCP1:
                // Legacy NetID identifier for production
            case CCP2:
                // Legacy NetID identifier for test
            case CCP8:
                return AuthenticationMethod.NET_ID;

            case CCP10:
            case CCP12:
                return AuthenticationMethod.BANK_ID;

            case CCP11:
            case CCP13:
                return AuthenticationMethod.MOBILT_BANK_ID;
        }
        throw new IllegalArgumentException("Could not parse AuthenticationMethod from SAML attribute 'LoginMethod': " + loginMethod);
    }
}
