/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by eriklupander on 2015-09-30.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElegRedirectFilterTest {

    private ElegRedirectFilter testee = new ElegRedirectFilter();

    @Test
    public void testRedirectsIfPropertySet() throws ServletException, IOException {
        testee.setElegIdpUrl("test");
        HttpServletResponse resp = mock(HttpServletResponse.class);
        testee.doFilterInternal(mock(HttpServletRequest.class), resp, mock(FilterChain.class));
        verify(resp, times(1)).sendRedirect(anyString());
    }

    @Test(expected = IllegalStateException.class)
    public void testNoIdpUrlConfiguredThrowsException() throws ServletException, IOException {
        testee.doFilterInternal(mock(HttpServletRequest.class), mock(HttpServletResponse.class), mock(FilterChain.class));
    }

}
