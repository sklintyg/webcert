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

package se.inera.intyg.webcert.web.auth.eleg;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import se.inera.intyg.webcert.web.auth.common.BaseSAMLCredentialTest;

/**
 * @author eriklupander
 */
public class CgiElegAssertionTest extends BaseSAMLCredentialTest {

    @BeforeClass
    public static void readSamlAssertions() throws Exception {
        bootstrapSamlAssertions();
    }

    @Test
    public void testAssertionWithEnhetAndVardgivare() {

        CgiElegAssertion assertion = new CgiElegAssertion(assertionPrivatlakare);

        assertEquals("197705232382", assertion.getPersonId());
        assertEquals("Frida", assertion.getFornamn());
        assertEquals("Kranstege", assertion.getEfternamn());

        assertEquals("Testbank A e-Customer CA2 for BankID", assertion.getUtfardareCANamn());
        assertEquals("Testbank A AB (publ)", assertion.getUtfardareOrganisationsNamn());
        assertEquals("3", assertion.getSecurityLevel());

        assertEquals("urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI", assertion.getAuthenticationScheme());
        assertEquals("ccp10", assertion.getLoginMethod());
    }
}
