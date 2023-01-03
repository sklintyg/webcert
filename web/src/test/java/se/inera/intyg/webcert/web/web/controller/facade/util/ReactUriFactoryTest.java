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
package se.inera.intyg.webcert.web.web.controller.facade.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ReactUriFactoryTest {

    private ReactUriFactory reactUriFactory;
    private UriInfo uriInfo;

    @BeforeEach
    void setUp() {
        reactUriFactory = new ReactUriFactory();

        uriInfo = mock(UriInfo.class);
        final var uriBuilder = UriBuilder.fromUri("https://wc.localtest.me/visa/xxxx-yyyyy-zzzzz-qqqqq/saved");
        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);

        ReflectionTestUtils.setField(reactUriFactory, "hostReactClient", "wc2.wc.localtest.me");
        ReflectionTestUtils.setField(reactUriFactory, "urlReactTemplate", "/certificate/{certId}");
    }

    @Test
    void shallReturnUriForCertificate() {
        final var certificateId = "xxxx-yyyyy-zzzzz-qqqqq";
        final var actualUri = reactUriFactory.uriForCertificate(uriInfo, certificateId);
        assertEquals("https://wc2.wc.localtest.me/certificate/xxxx-yyyyy-zzzzz-qqqqq", actualUri.toString());
    }
}