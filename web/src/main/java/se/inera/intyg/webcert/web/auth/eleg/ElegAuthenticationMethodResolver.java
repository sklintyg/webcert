/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;

@Service
public class ElegAuthenticationMethodResolver {

    public AuthenticationMethod resolveAuthenticationMethod(String loginMethod) {

        if (loginMethod == null) {
            throw new IllegalArgumentException("Authentication method must not be null");
        }

        ElegLoginMethod loginMethodEnum;
        try {
            loginMethodEnum = ElegLoginMethod.valueOf(loginMethod.toUpperCase());

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failure parsing AuthenticationMethod '%s' received in SAML attribute 'LoginMethod'."
                .formatted(loginMethod), e);
        }
        return switch (loginMethodEnum) {
            case CCP1, CCP2, CCP8 -> AuthenticationMethod.NET_ID;
            case CCP10, CCP12 -> AuthenticationMethod.BANK_ID;
            case CCP11, CCP13, CCP19, CCP28 -> AuthenticationMethod.MOBILT_BANK_ID;
        };
    }
}
