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
package se.inera.intyg.webcert.web.auth;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@RunWith(MockitoJUnitRunner.class)
public class CustomXFrameOptionsHeaderWriterTest {

    @Mock
    private WebCertUserService webcertUserService;

    @Mock
    private WebCertUser webcertUser;

    @InjectMocks
    private CustomXFrameOptionsHeaderWriter customXFrameOptionsHeaderWriter;

    private static final String XFRAME_OPTIONS_HEADER = "X-Frame-Options";

    private final static String REQUEST_URI = "https://test.test/test";
    private final static String REQUEST_URI_PDF_ENDPOINT = "https://test.test/pdf";

    private static final boolean HAS_AUTHENTICATION_CONTEXT = true;
    private static final boolean NO_AUTHENTICATION_CONTEXT = false;

    @Test
    public void shouldSkipHeaderWhenNoAuthenticationContext() {
        final var httpServerRequest = createHttpServerRequest(REQUEST_URI);
        final var httpServerResponse = createHttpServerResponse();

        setMockReturnValueForAuthenticationContext(NO_AUTHENTICATION_CONTEXT);

        customXFrameOptionsHeaderWriter.writeHeaders(httpServerRequest, httpServerResponse);

        assertFalse(httpServerResponse.containsHeader(XFRAME_OPTIONS_HEADER));
    }

    @Test
    public void shouldSkipHeaderWhenRequestForPdfEndpoint() {
        final var httpServerRequest = createHttpServerRequest(REQUEST_URI_PDF_ENDPOINT);
        final var httpServerResponse = createHttpServerResponse();

        setMockReturnValueForAuthenticationContext(HAS_AUTHENTICATION_CONTEXT);

        customXFrameOptionsHeaderWriter.writeHeaders(httpServerRequest, httpServerResponse);

        assertFalse(httpServerResponse.containsHeader(XFRAME_OPTIONS_HEADER));
    }

    @Test
    public void shouldNotSkipHeaderWhenHasAuthenticationContexIsReadonlyAndNotPdfEndpoint() {
        final var httpServerRequest = createHttpServerRequest(REQUEST_URI);
        final var httpServerResponse = createHttpServerResponse();

        setMockReturnValueForAuthenticationContext(HAS_AUTHENTICATION_CONTEXT);

        customXFrameOptionsHeaderWriter.writeHeaders(httpServerRequest, httpServerResponse);

        assertTrue(httpServerResponse.containsHeader(XFRAME_OPTIONS_HEADER));
        assertEquals("DENY", httpServerResponse.getHeader(XFRAME_OPTIONS_HEADER));
    }

    private HttpServletRequest createHttpServerRequest(String requestUri) {
        final var request = new MockHttpServletRequest();
        request.setRequestURI(requestUri);
        return request;
    }

    private HttpServletResponse createHttpServerResponse() {
        return new MockHttpServletResponse();
    }

    private void setMockReturnValueForAuthenticationContext(boolean hasContext) {
        when(webcertUserService.hasAuthenticationContext()).thenReturn(hasContext);
    }
}
