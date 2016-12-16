/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SPRING_SECURITY_SAVED_REQUEST_KEY;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;

/**
 * Created by Magnus Ekstrand on 03/12/15.
 */
public class WebCertUserOriginTest {

    private WebCertUserOrigin webcertUserOrigin = new WebCertUserOrigin();

    @Test
    public void testDjupintegrationRegexp() throws Exception {
        assertTrue("/visa/intyg/99aaa4f1-d862-4750-a628-f7dcb9c8bac0".matches(WebCertUserOrigin.REGEXP_REQUESTURI_DJUPINTEGRATION));
        assertTrue("/visa/intyg/99aaa4f1-d862-4750-a628-f7dcb9c8bac0/".matches(WebCertUserOrigin.REGEXP_REQUESTURI_DJUPINTEGRATION));
    }

    @Test
    public void testUthoppRegexp() throws Exception {
        assertTrue("/webcert/web/user/certificate/99aaa4f1-d862-4750-a628-f7dcb9c8bac0/questions".matches(WebCertUserOrigin.REGEXP_REQUESTURI_UTHOPP));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolveOriginRequestNull() {
        webcertUserOrigin.resolveOrigin(null);
    }

    @Test
    public void testResolveOriginNormal() {
        String res = webcertUserOrigin.resolveOrigin(buildRequest("non/matching/url"));

        assertEquals("NORMAL", res);
    }

    @Test
    public void testResolveOriginNormalNoSavedRequest() {
        String res = webcertUserOrigin.resolveOrigin(buildRequest(null));

        assertEquals("NORMAL", res);
    }

    @Test
    public void testResolveOriginDjupintegration() {
        String res = webcertUserOrigin.resolveOrigin(buildRequest("/visa/intyg/luse/99aaa4f1-d862-4750-a628-f7dcb9c8bac0"));

        assertEquals("DJUPINTEGRATION", res);
    }

    @Test
    public void testResolveOriginUthopp() {
        String res = webcertUserOrigin.resolveOrigin(buildRequest("/webcert/web/user/certificate/luse/99aaa4f1-d862-4750-a628-f7dcb9c8bac0/questions"));

        assertEquals("UTHOPP", res);
    }

    private HttpServletRequest buildRequest(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class));
        if (uri != null) {
            DefaultSavedRequest defaultSavedRequest = mock(DefaultSavedRequest.class);
            when(defaultSavedRequest.getRequestURI()).thenReturn(uri);
            when(request.getSession().getAttribute(SPRING_SECURITY_SAVED_REQUEST_KEY)).thenReturn(defaultSavedRequest);
        }
        return request;
    }

}


