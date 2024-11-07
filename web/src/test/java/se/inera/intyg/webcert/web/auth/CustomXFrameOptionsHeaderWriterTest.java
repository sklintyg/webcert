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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomXFrameOptionsHeaderWriterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CustomXFrameOptionsHeaderWriter customXFrameOptionsHeaderWriter;

    private static final int ONE = 1;
    private static final String DENY = "DENY";
    private static final String XFRAME_OPTIONS_HEADER = "X-Frame-Options";
    private static final String REQUEST_URI = "https://test.test/test";
    private static final String REQUEST_URI_PDF_ENDPOINT = "https://test.test/pdf";

    @Test
    void shouldAddHeaderForNonPdfEndpoint() {
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        customXFrameOptionsHeaderWriter.writeHeaders(request, response);
        verify(response, times(ONE)).addHeader(XFRAME_OPTIONS_HEADER, DENY);
    }

    @Test
    void shouldSkipHeaderWhenRequestForPdfEndpoint() {
        when(request.getRequestURI()).thenReturn(REQUEST_URI_PDF_ENDPOINT);
        customXFrameOptionsHeaderWriter.writeHeaders(request, response);
        verifyNoInteractions(response);
    }

}