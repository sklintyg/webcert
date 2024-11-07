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
package se.inera.intyg.webcert.web.auth.eleg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;

@ExtendWith(MockitoExtension.class)
class ElegAuthenticationMethodResolverTest  {

    private static final String MOBILT_BANK_ID_LOGIN_METHOD = "ccp11";
    private static final String MOBILT_BANK_ID_STATIC_QR_CODE = "ccp19";
    private static final String MOBILT_BANK_ID_NON_STATIC_QR_CODE = "ccp28";
    private static final String BANK_ID_LOGIN_METHOD = "ccp10";
    private static final String NET_ID_LOGIN_METHOD = "ccp8";
    private static final String INDETERMINATE_LOGIN_METHOD = "";
    private static final String UNKNOWN_LOGIN_METHOD = "ccp7";

    @InjectMocks
    private ElegAuthenticationMethodResolver elegAuthenticationMethodResolver;


   @Test
    void testBankID() {
        AuthenticationMethod authMetod = elegAuthenticationMethodResolver.resolveAuthenticationMethod(BANK_ID_LOGIN_METHOD);
        assertEquals(AuthenticationMethod.BANK_ID, authMetod);
    }

    @Test
    void testMobiltBankIDCCP11() {
        AuthenticationMethod authMetod = elegAuthenticationMethodResolver.resolveAuthenticationMethod(MOBILT_BANK_ID_LOGIN_METHOD);
        assertEquals(AuthenticationMethod.MOBILT_BANK_ID, authMetod);
    }

    @Test
    void testMobiltBankIDCCP19() {
        AuthenticationMethod authMetod = elegAuthenticationMethodResolver.resolveAuthenticationMethod(MOBILT_BANK_ID_STATIC_QR_CODE);
        assertEquals(AuthenticationMethod.MOBILT_BANK_ID, authMetod);
    }

    @Test
    void testMobiltBankIDCCP28() {
        AuthenticationMethod authMetod = elegAuthenticationMethodResolver.resolveAuthenticationMethod(MOBILT_BANK_ID_NON_STATIC_QR_CODE);
        assertEquals(AuthenticationMethod.MOBILT_BANK_ID, authMetod);
    }

    @Test
    void testNetID() {
        AuthenticationMethod authMetod = elegAuthenticationMethodResolver.resolveAuthenticationMethod(NET_ID_LOGIN_METHOD);
        assertEquals(AuthenticationMethod.NET_ID, authMetod);
    }

    @Test
    void testNoIssuerThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            elegAuthenticationMethodResolver.resolveAuthenticationMethod(null));
    }

    @Test
    void testIndeterminateIssuerThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            elegAuthenticationMethodResolver.resolveAuthenticationMethod(INDETERMINATE_LOGIN_METHOD));
    }

    @Test
    void testUnknwonIssuerThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            elegAuthenticationMethodResolver.resolveAuthenticationMethod(UNKNOWN_LOGIN_METHOD));
    }
}
