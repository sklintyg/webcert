/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.RequestCache;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.IntygIntegrationController;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebcertAuthenticationSuccessHandlerTest {

    private static final String PNR = "20121212-1212";
    private static final String URL = "/visa/intyg/intyg-123";

    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse resp;

    @Mock
    private Authentication auth;

    @Mock
    private RequestCache requestCache;

    @Mock
    private DefaultSavedRequest savedRequest;

    @Mock
    private WebCertUser user;

    @Mock
    private RedirectStrategy redirectStrategy;

    @InjectMocks
    private WebcertAuthenticationSuccessHandler testee;

    @Before
    public void init() {
        when(savedRequest.getMethod()).thenReturn("GET");
        when(savedRequest.getRedirectUrl()).thenReturn(URL);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testOkNoSaved() throws ServletException, IOException {
        when(requestCache.getRequest(req, resp)).thenReturn(null);
        RedirectStrategy redirectStrategy = mock(RedirectStrategy.class);
        testee.setRedirectStrategy(redirectStrategy);

        testee.onAuthenticationSuccess(req, resp, auth);
        verify(redirectStrategy, times(1)).sendRedirect(any(HttpServletRequest.class), any(HttpServletResponse.class), anyString());
    }

    @Test
    public void testOkWithSavedGET() throws ServletException, IOException {
        when(requestCache.getRequest(req, resp)).thenReturn(savedRequest);
        RedirectStrategy redirectStrategy = mock(RedirectStrategy.class);
        testee.setRedirectStrategy(redirectStrategy);

        testee.onAuthenticationSuccess(req, resp, auth);
        verify(redirectStrategy, times(1)).sendRedirect(any(HttpServletRequest.class), any(HttpServletResponse.class), eq(URL));
    }

    @Test
    public void testOkWithSavedPOST() throws ServletException, IOException {
        when(savedRequest.getMethod()).thenReturn("POST");
        when(savedRequest.getRequestURI()).thenReturn(URL);
        when(savedRequest.getParameterMap()).thenReturn(buildParameterMap());
        when(requestCache.getRequest(req, resp)).thenReturn(savedRequest);

        testee.onAuthenticationSuccess(req, resp, auth);
        verify(redirectStrategy, times(1)).sendRedirect(any(HttpServletRequest.class), any(HttpServletResponse.class), eq(URL + "/saved"));

        ArgumentCaptor<IntegrationParameters> captor = ArgumentCaptor.forClass(IntegrationParameters.class);
        verify(user).setParameters(captor.capture());

        assertEquals(PNR, captor.getValue().getAlternateSsn());
        assertTrue(captor.getValue().isPatientDeceased());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSavedPOSTFailsWhenUserHasParameters() throws ServletException, IOException {
        when(savedRequest.getMethod()).thenReturn("POST");
        when(savedRequest.getRequestURI()).thenReturn(URL);
        when(requestCache.getRequest(req, resp)).thenReturn(savedRequest);
        when(user.getParameters()).thenReturn(IntegrationParameters.of("", "", "", "", "", "", "", "", "", false, false, false, false));

        testee.onAuthenticationSuccess(req, resp, auth);
        verifyZeroInteractions(redirectStrategy);
    }

    private Map<String, String[]> buildParameterMap() {
        Map<String, String[]> map = new HashMap<>();
        map.put(IntygIntegrationController.PARAM_PATIENT_ALTERNATE_SSN, new String[] { PNR });
        map.put(IntygIntegrationController.PARAM_PATIENT_DECEASED, new String[] { "true" });
        return map;
    }
}
