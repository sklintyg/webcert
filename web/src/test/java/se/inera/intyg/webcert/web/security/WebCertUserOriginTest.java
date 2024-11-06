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
package se.inera.intyg.webcert.web.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

@RunWith(MockitoJUnitRunner.class)
public class WebCertUserOriginTest {

    @Mock
    private RequestCache requestCache;

    @InjectMocks
    private WebCertUserOrigin webcertUserOrigin;

    @Test
    public void testDjupintegrationRegexp() {
        assertTrue("/visa/intyg/99aaa4f1-d862-4750-a628-f7dcb9c8bac0".matches(WebCertUserOrigin.REGEXP_REQUESTURI_DJUPINTEGRATION));
        assertTrue("/visa/intyg/99aaa4f1-d862-4750-a628-f7dcb9c8bac0/".matches(WebCertUserOrigin.REGEXP_REQUESTURI_DJUPINTEGRATION));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolveOriginRequestNull() {
        webcertUserOrigin.resolveOrigin(null);
    }

    @Test
    public void testResolveOriginNormal() {
        final var res = webcertUserOrigin.resolveOrigin(buildRequest("non/matching/url"));
        assertEquals("NORMAL", res);
    }

    @Test
    public void testResolveOriginNormalNoSavedRequest() {
        when(requestCache.getRequest(any(HttpServletRequest.class), isNull())).thenReturn(null);

        final var res = webcertUserOrigin.resolveOrigin(buildRequest(null));
        assertEquals("NORMAL", res);
    }

    @Test
    public void testResolveOriginDjupintegrationFromRedisSavedRequest() {
        SavedRequest defaultSavedRequest = mock(DefaultSavedRequest.class);
        when(defaultSavedRequest.getRedirectUrl()).thenReturn("/visa/intyg/luse/99aaa4f1-d862-4750-a628-f7dcb9c8bac0");
        when(requestCache.getRequest(any(HttpServletRequest.class), isNull())).thenReturn(defaultSavedRequest);

        final var res = webcertUserOrigin.resolveOrigin(buildRequest(null));
        assertEquals("DJUPINTEGRATION", res);
    }

    @Test
    public void testResolveOriginDjupintegration() {
        final var res = webcertUserOrigin.resolveOrigin(buildRequest("/visa/intyg/luse/99aaa4f1-d862-4750-a628-f7dcb9c8bac0"));
        assertEquals("DJUPINTEGRATION", res);
    }

    private HttpServletRequest buildRequest(String uri) {
        final var request = mock(HttpServletRequest.class);
        if (uri != null) {
            final var defaultSavedRequest = mock(DefaultSavedRequest.class);
            when(defaultSavedRequest.getRedirectUrl()).thenReturn(uri);
            when(requestCache.getRequest(any(HttpServletRequest.class), isNull())).thenReturn(defaultSavedRequest);
        }
        return request;
    }

}
