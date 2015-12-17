/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.saml.SAMLCredential;

import se.inera.intyg.webcert.web.auth.eleg.ElegWebCertUserDetailsService;
import se.inera.intyg.webcert.web.security.WebCertUserDetailsService;

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
@RunWith(MockitoJUnitRunner.class)
public class UnifiedUserDetailsServiceTest extends BaseSAMLCredentialTest {

    @Mock
    private ElegWebCertUserDetailsService elegWebCertUserDetailsService;

    @Mock
    private WebCertUserDetailsService webCertUserDetailsService;

    @InjectMocks
    private UnifiedUserDetailsService unifiedUserDetailsService;

    @BeforeClass
    public static void readSamlAssertions() throws Exception {
        bootstrapSamlAssertions();
    }

    @Test
    public void testSoftwarePKI() {
        unifiedUserDetailsService.loadUserBySAML(buildPrivatlakareSamlCredential());
        verify(elegWebCertUserDetailsService, times(1)).loadUserBySAML(any(SAMLCredential.class));
    }

    @Test
    public void testTLSClient() {
        unifiedUserDetailsService.loadUserBySAML(buildLandstingslakareSamlCredential());
        verify(webCertUserDetailsService, times(1)).loadUserBySAML(any(SAMLCredential.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownAuthContext() {
        unifiedUserDetailsService.loadUserBySAML(buildUnknownSamlCredential());
        verify(webCertUserDetailsService, times(0)).loadUserBySAML(any(SAMLCredential.class));
    }

}
