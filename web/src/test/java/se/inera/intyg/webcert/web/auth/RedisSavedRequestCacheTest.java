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
package se.inera.intyg.webcert.web.auth;

import java.util.concurrent.TimeUnit;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

import com.helger.commons.collection.iterate.EmptyEnumeration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RedisSavedRequestCacheTest {

    private static final String HTTP_SESSION_ID = "session-1";
    private static final String COOKIE_SESSION_ID = "cookie-session-1";

    private static final String PREFIX = "webcert:savedrequests:";

    @Mock
    private ValueOperations<String, DefaultSavedRequest> valueOps;

    @InjectMocks
    private RedisSavedRequestCache testee;

    @Before
    public void init() {
        testee.setRequestMatcher(new RegexRequestMatcher("/web/.*|/visa/.*|/webcert/.*", null));
    }

    @Test
    public void storeRequestWithIdFromHttpSession() {
        testee.saveRequest(mockReq(HTTP_SESSION_ID, null), mockResp());

        verify(valueOps, times(1)).set(eq(PREFIX + HTTP_SESSION_ID), any(DefaultSavedRequest.class), anyLong(),
                eq(TimeUnit.MINUTES));
    }

    @Test
    public void storeRequestWithIdFromSpringSession() {
        testee.saveRequest(mockReq(HTTP_SESSION_ID, COOKIE_SESSION_ID), mockResp());

        verify(valueOps, times(1)).set(eq(PREFIX + HTTP_SESSION_ID), any(DefaultSavedRequest.class), anyLong(),
                eq(TimeUnit.MINUTES));
    }

    @Test
    public void storeRequestDoesNothingIfRequestDoesNotMatch() {
        testee.setRequestMatcher(new RegexRequestMatcher("/other/.*", null));
        testee.saveRequest(mockReq(HTTP_SESSION_ID, COOKIE_SESSION_ID), mockResp());

        verifyZeroInteractions(valueOps);
    }

    @Test
    public void getRequestReturnsSavedRequestWhenAvailable() {
        DefaultSavedRequest mockedSavedRequest = mock(DefaultSavedRequest.class);
        when(valueOps.get(PREFIX + HTTP_SESSION_ID)).thenReturn(mockedSavedRequest);
        SavedRequest request = testee.getRequest(mockReq(HTTP_SESSION_ID, COOKIE_SESSION_ID), mockResp());
        assertNotNull(request);
    }

    @Test
    public void getRequestReturnsNullWhenNoRequestSaved() {
        DefaultSavedRequest mockedSavedRequest = mock(DefaultSavedRequest.class);
        when(valueOps.get(PREFIX + HTTP_SESSION_ID)).thenReturn(null);
        SavedRequest request = testee.getRequest(mockReq(HTTP_SESSION_ID, COOKIE_SESSION_ID), mockResp());
        assertNull(request);
    }

    private HttpServletResponse mockResp() {
        return mock(HttpServletResponse.class);
    }

    private HttpServletRequest mockReq(String httpSessionId, String springSessionId) {
        StringBuffer sb = new StringBuffer();
        sb.append("/visa/intyg");

        HttpSession session = mock(HttpSession.class);
        when(session.getId()).thenReturn(httpSessionId);

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeaderNames()).thenReturn(new EmptyEnumeration<>());
        when(req.getLocales()).thenReturn(new EmptyEnumeration<>());
        when(req.getScheme()).thenReturn("https");
        when(req.getRequestURL()).thenReturn(sb);
        when(req.getSession()).thenReturn(session);
        when(req.getServletPath()).thenReturn("/visa/intyg");

        if (springSessionId != null) {
            when(req.getCookies()).thenReturn(buildCookies(springSessionId));
        }
        return req;
    }

    private Cookie[] buildCookies(String springSessionId) {
        return new Cookie[]{new Cookie("SESSION", springSessionId)};
    }
}
