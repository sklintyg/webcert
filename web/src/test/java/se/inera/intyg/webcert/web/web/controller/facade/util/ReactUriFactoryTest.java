/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;

class ReactUriFactoryTest {

  private ReactUriFactory reactUriFactory;
  private MockHttpServletRequest mockRequest;

  @BeforeEach
  void setUp() {
    reactUriFactory = new ReactUriFactory();

    mockRequest = new MockHttpServletRequest();
    mockRequest.setScheme("https");
    mockRequest.setServerName("wc.localtest.me");
    mockRequest.setServerPort(443);
    mockRequest.setRequestURI("/visa/xxxx-yyyyy-zzzzz-qqqqq/saved");

    ReflectionTestUtils.setField(reactUriFactory, "webcertDomainName", "wc.localtest.me");
    ReflectionTestUtils.setField(reactUriFactory, "urlReactTemplate", "/certificate/{certId}");
    ReflectionTestUtils.setField(
        reactUriFactory, "urlReactSignErrorTemplate", "/certificate/{certId}/sign/{error}");
    ReflectionTestUtils.setField(
        reactUriFactory, "urlReactQuestionsTemplate", "/certificate/{certId}/questions");
    ReflectionTestUtils.setField(reactUriFactory, "urlReactErrorTemplate", "/error");
    ReflectionTestUtils.setField(
        reactUriFactory,
        "urlReactUnitSelectionTemplate",
        "/certificate/{certId}/launch-unit-selection");
  }

  @Test
  void shallReturnUriForCertificate() {
    final var certificateId = "xxxx-yyyyy-zzzzz-qqqqq";
    final var actualUri = reactUriFactory.uriForCertificate(mockRequest, certificateId);
    assertEquals(
        actualUri.toString(), "https://wc.localtest.me/certificate/xxxx-yyyyy-zzzzz-qqqqq");
  }

  @Test
  void shallReturnUriWithSignErrorForCertificate() {
    final var certificateId = "xxxx-yyyyy-zzzzz-qqqqq";
    final var actualUri =
        reactUriFactory.uriForCertificateWithSignError(
            mockRequest, certificateId, SignaturStatus.ERROR);
    assertEquals(
        actualUri.toString(),
        "https://wc.localtest.me/certificate/xxxx-yyyyy-zzzzz-qqqqq/sign/error");
  }

  @Test
  void shallReturnUriForCertificateQuestions() {
    final var certificateId = "xxxx-yyyyy-zzzzz-qqqqq";
    final var actualUri = reactUriFactory.uriForCertificateQuestions(mockRequest, certificateId);
    assertEquals(
        actualUri.toString(),
        "https://wc.localtest.me/certificate/xxxx-yyyyy-zzzzz-qqqqq/questions");
  }

  @Test
  void shallReturnUriForError() {
    final var reason = "auth-exception";
    final var actualUri = reactUriFactory.uriForErrorResponse(mockRequest, reason);
    assertEquals(actualUri.toString(), "https://wc.localtest.me/error?reason=auth-exception");
  }

  @Test
  void shallReturnUriForUnitSelection() {
    final var certificateId = "xxxx-yyyyy-zzzzz-qqqqq";
    final var actualUri = reactUriFactory.uriForUnitSelection(mockRequest, certificateId);
    assertEquals(
        actualUri.toString(),
        "https://wc.localtest.me/certificate/xxxx-yyyyy-zzzzz-qqqqq/launch-unit-selection");
  }
}
