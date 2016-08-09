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

package se.inera.intyg.webcert.web.auth.eleg;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.saml.SAMLCredential;

import se.inera.intyg.common.security.common.model.AuthenticationMethod;
import se.inera.intyg.webcert.web.auth.common.BaseSAMLCredentialTest;

/**
 * Created by eriklupander on 2015-08-26.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElegAuthenticationMethodResolverTest extends BaseSAMLCredentialTest {

    private static final String MOBILT_BANK_ID_LOGIN_METHOD = "ccp11";
    private static final String BANK_ID_LOGIN_METHOD = "ccp10";
    private static final String NET_ID_LOGIN_METHOD = "ccp8";
    private static final String INDETERMINATE_LOGIN_METHOD = "";
    private static final String UNKNOWN_LOGIN_METHOD = "ccp7";

    @Mock
    private ElegAuthenticationAttributeHelper elegAuthenticationAttributeHelper;

    @InjectMocks
    private ElegAuthenticationMethodResolverImpl testee;

    @BeforeClass
    public static void setup() throws Exception {
        bootstrapSamlAssertions();

    }

    @Test
    public void testBankID() {
        when(elegAuthenticationAttributeHelper.getAttribute(any(SAMLCredential.class), anyString())).thenReturn(BANK_ID_LOGIN_METHOD);
        AuthenticationMethod authMetod = testee.resolveAuthenticationMethod(buildPrivatlakareSamlCredential());
        assertEquals(AuthenticationMethod.BANK_ID, authMetod);
    }

    @Test
    public void testMobiltBankID() {
        when(elegAuthenticationAttributeHelper.getAttribute(any(SAMLCredential.class), anyString())).thenReturn(MOBILT_BANK_ID_LOGIN_METHOD);
        AuthenticationMethod authMetod = testee.resolveAuthenticationMethod(buildPrivatlakareSamlCredential());
        assertEquals(AuthenticationMethod.MOBILT_BANK_ID, authMetod);
    }

    @Test
    public void testNetID() {
        when(elegAuthenticationAttributeHelper.getAttribute(any(SAMLCredential.class), anyString())).thenReturn(NET_ID_LOGIN_METHOD);
        AuthenticationMethod authMetod = testee.resolveAuthenticationMethod(buildPrivatlakareSamlCredential());
        assertEquals(AuthenticationMethod.NET_ID, authMetod);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoIssuerThrowsException() {
        when(elegAuthenticationAttributeHelper.getAttribute(any(SAMLCredential.class), anyString())).thenReturn(null);
        testee.resolveAuthenticationMethod(buildPrivatlakareSamlCredential());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndeterminateIssuerThrowsException() {
        when(elegAuthenticationAttributeHelper.getAttribute(any(SAMLCredential.class), anyString())).thenReturn(INDETERMINATE_LOGIN_METHOD);
        testee.resolveAuthenticationMethod(buildPrivatlakareSamlCredential());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknwonIssuerThrowsException() {
        when(elegAuthenticationAttributeHelper.getAttribute(any(SAMLCredential.class), anyString())).thenReturn(UNKNOWN_LOGIN_METHOD);
        testee.resolveAuthenticationMethod(buildPrivatlakareSamlCredential());
    }
}
