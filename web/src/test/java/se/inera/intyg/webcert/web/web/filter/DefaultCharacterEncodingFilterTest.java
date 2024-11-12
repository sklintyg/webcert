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
package se.inera.intyg.webcert.web.web.filter;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * These tests check if {@link DefaultCharacterEncodingFilter} correctly sets encoding to requests.
 * When no encoding is set the filter should set request character encoding to UTF-8.
 */

@RunWith(MockitoJUnitRunner.class)
public class DefaultCharacterEncodingFilterTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private DefaultCharacterEncodingFilter testee = new DefaultCharacterEncodingFilter();

    @Test
    public void testNoEncodingSet() throws ServletException, IOException {
        testee.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletRequest, times(1)).setCharacterEncoding("UTF-8");
    }

    @Test
    public void testEncodingSet() throws IOException, ServletException {
        when(httpServletRequest.getCharacterEncoding()).thenReturn("UTF-8");

        testee.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletRequest, times(0)).setCharacterEncoding("UTF-8");
    }
}
