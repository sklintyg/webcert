/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.maillink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class MailLinkServiceImplTest {

    private static final Object URL_BASE_TEMPLATE = "url/base/template";
    private static final Object URL_UTKAST_FRAGMENT_TEMPLATE = "/{certType}/edit/{certId}?patientId={patientId}&hospName={hospName}";

    private MailLinkServiceImpl service = new MailLinkServiceImpl();

    @Before
    public void setup() {
        ReflectionTestUtils.setField(service, "urlBaseTemplate", URL_BASE_TEMPLATE);
        ReflectionTestUtils.setField(service, "urlUtkastFragmentTemplate", URL_UTKAST_FRAGMENT_TEMPLATE);
    }

    @Test
    public void testIntygRedirectIntygIdMissing() {
        URI res = service.intygRedirect("typ", null);

        assertNull(res);
    }

    @Test
    public void testIntygRedirectTypMissing() {
        URI res = service.intygRedirect(null, "intygId");

        assertNull(res);
    }

    @Test
    public void testIntygRedirect() {
        final String typ = "typ";
        final String intygId = "intyg-id";
        URI res = service.intygRedirect(typ, intygId);

        assertNotNull(res);
        assertEquals(URL_BASE_TEMPLATE, res.getPath());
        assertEquals("/typ/edit/intyg-id?patientId=&hospName=", res.getFragment());
    }
}
