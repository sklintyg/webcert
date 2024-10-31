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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.auth.eleg.ElegWebCertUserDetailsService;

/**
 * Tests so the unified userdetails service forwards the SAMLCredential to the correct underlying userDetailsService
 * depending on authContextClassRef.
 *
 * That may have to change if we for example have privatläkare using BankID on card which also could be a TLSClient. In
 * that case, the UnifiedUserDetailsService will have to be rewritten to introspect some other attribute on the SAMLCredential
 * in order to route the request correctly.
 *
 * Created by eriklupander on 2015-08-20.
 */
@ExtendWith(MockitoExtension.class)
class UnifiedUserDetailsServiceTest  {

    private static final int ONE = 1;
    private static final String USER_ID = "userId";
    private static final String UNKNOWN = "unknown";
    private static final String SAMBI_LOA3 = "http://id.sambi.se/loa/loa3";
    private static final String SOFTWARE_PKI = "urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI";
    private static final String SMARTCARD_PKI = "urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI";
    private static final String MOBILITY_TWO_FACTOR_CONTRACT = "urn:oasis:names:tc:SAML:2.0:ac:classes:MobileTwofactorContract";

    @Mock
    private ElegWebCertUserDetailsService elegWebCertUserDetailsService;
    @Mock
    private WebcertUserDetailsService webCertUserDetailsService;

    @InjectMocks
    private UnifiedUserDetailsService unifiedUserDetailsService;

    @Test
    void testSoftwarePKI() {
        unifiedUserDetailsService.buildUserPrincipal(USER_ID, SOFTWARE_PKI);
        verify(elegWebCertUserDetailsService, times(ONE)).buildUserPrincipal(USER_ID, SOFTWARE_PKI);
    }

    @Test
    void testSmartcardPKI() {
        unifiedUserDetailsService.buildUserPrincipal(USER_ID, SMARTCARD_PKI);
        verify(elegWebCertUserDetailsService, times(ONE)).buildUserPrincipal(USER_ID, SMARTCARD_PKI);
    }

    @Test
    void testMobileTwoFactorContract() {
        unifiedUserDetailsService.buildUserPrincipal(USER_ID, MOBILITY_TWO_FACTOR_CONTRACT);
        verify(elegWebCertUserDetailsService, times(ONE)).buildUserPrincipal(USER_ID, MOBILITY_TWO_FACTOR_CONTRACT);
    }

    @Test
    void testTLSClient() {
        unifiedUserDetailsService.buildUserPrincipal(USER_ID, SAMBI_LOA3);
        verify(webCertUserDetailsService, times(ONE)).buildUserPrincipal(USER_ID, SAMBI_LOA3);
    }

    @Test
    void testUnknownAuthContext() {
        assertThrows(IllegalArgumentException.class, () -> unifiedUserDetailsService.buildUserPrincipal(USER_ID, UNKNOWN));
    }

}
