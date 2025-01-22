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
package se.inera.intyg.webcert.web.web.controller.facade.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;

class ReactUriFactoryTest {

    private ReactUriFactory reactUriFactory;
    private UriInfo uriInfo;

    @BeforeEach
    void setUp() {
        reactUriFactory = new ReactUriFactory();

        uriInfo = mock(UriInfo.class);
        final var uriBuilder = UriBuilder.fromUri("https://wc.localtest.me/visa/xxxx-yyyyy-zzzzz-qqqqq/saved");
        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);

        ReflectionTestUtils.setField(reactUriFactory, "webcertDomainName", "wc.localtest.me");
        ReflectionTestUtils.setField(reactUriFactory, "urlReactTemplate", "/certificate/{certId}");
        ReflectionTestUtils.setField(reactUriFactory, "urlReactSignErrorTemplate", "/certificate/{certId}/sign/{error}");
        ReflectionTestUtils.setField(reactUriFactory, "urlReactQuestionsTemplate", "/certificate/{certId}/questions");
        ReflectionTestUtils.setField(reactUriFactory, "urlReactErrorTemplate", "/error");
        ReflectionTestUtils.setField(reactUriFactory, "urlReactUnitSelectionTemplate", "/certificate/{certId}/launch-unit-selection");
    }

    @Test
    void shallReturnUriForCertificate() {
        final var certificateId = "xxxx-yyyyy-zzzzz-qqqqq";
        final var actualUri = reactUriFactory.uriForCertificate(uriInfo, certificateId);
        assertEquals("https://wc.localtest.me/certificate/xxxx-yyyyy-zzzzz-qqqqq", actualUri.toString());
    }

    @Test
    void shallReturnUriWithSignErrorForCertificate() {
        final var certificateId = "xxxx-yyyyy-zzzzz-qqqqq";
        final var actualUri = reactUriFactory.uriForCertificateWithSignError(uriInfo, certificateId, SignaturStatus.ERROR);
        assertEquals("https://wc.localtest.me/certificate/xxxx-yyyyy-zzzzz-qqqqq/sign/error", actualUri.toString());
    }

    @Test
    void shallReturnUriForCertificateQuestions() {
        final var certificateId = "xxxx-yyyyy-zzzzz-qqqqq";
        final var actualUri = reactUriFactory.uriForCertificateQuestions(uriInfo, certificateId);
        assertEquals("https://wc.localtest.me/certificate/xxxx-yyyyy-zzzzz-qqqqq/questions", actualUri.toString());
    }

    @Test
    void shallReturnUriForError() {
        final var reason = "auth-exception";
        final var actualUri = reactUriFactory.uriForErrorResponse(uriInfo, reason);
        assertEquals("https://wc.localtest.me/error?reason=auth-exception", actualUri.toString());
    }

    @Test
    void shallReturnUriForUnitSelection() {
        final var certificateId = "xxxx-yyyyy-zzzzz-qqqqq";
        final var actualUri = reactUriFactory.uriForUnitSelection(uriInfo, certificateId);
        assertEquals("https://wc.localtest.me/certificate/xxxx-yyyyy-zzzzz-qqqqq/launch-unit-selection", actualUri.toString());
    }
}
