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
package se.inera.intyg.webcert.web.auth;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.IntygIntegrationController;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationSuccessHandlerTest {

    private static final int ONE = 1;
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String PNR = "20121212-1212";
    private static final String LAUNCH_ID = "97f279ba-7d2b-4b0a-8665-7adde08f26f4";
    private static final String REDIRECT_URL = "/visa/intyg/1ff83a34-1f90-4859-90db-2a1ec65dd741";
    private static final String WEBCERT_DOMAIN_NAME = "webcertDomainName";
    private static final String WEBCERT_DOMAIN_NAME_VALUE = "webcert.domain.name";

    @Mock
    private HttpServletRequest req;
    @Mock
    private HttpServletResponse resp;
    @Mock
    private RequestCache requestCache;
    @Mock
    private DefaultSavedRequest savedRequest;
    @Mock
    private WebCertUser user;
    @Mock
    private RedirectStrategy redirectStrategy;
    @Mock
    private Authentication authentication;
    @Mock
    private HttpSession session;

    @Captor
    private ArgumentCaptor<IntegrationParameters> captor = ArgumentCaptor.forClass(IntegrationParameters.class);

    @InjectMocks
    private CustomAuthenticationSuccessHandler successHandler;

    @Nested
    class TestsWitnNoOrGetSavedRequest {

        @BeforeEach
        void init() {
            ReflectionTestUtils.setField(successHandler, WEBCERT_DOMAIN_NAME, WEBCERT_DOMAIN_NAME_VALUE);
            successHandler.setRedirectStrategy(redirectStrategy);
        }

        @Test
        void shouldRedirectToOriginalWhenNoSavedRequest() throws ServletException, IOException {
            when(requestCache.getRequest(req, resp)).thenReturn(null);
            successHandler.onAuthenticationSuccess(req, resp, authentication);
            verify(redirectStrategy, times(ONE)).sendRedirect(req, resp, "/");
        }

        @Test
        void shouldRedirectWhenGetReqest() throws ServletException, IOException {
            when(requestCache.getRequest(req, resp)).thenReturn(savedRequest);
            when(savedRequest.getRedirectUrl()).thenReturn(REDIRECT_URL);
            when(savedRequest.getMethod()).thenReturn(GET);
            successHandler.onAuthenticationSuccess(req, resp, authentication);
            verify(redirectStrategy, times(ONE)).sendRedirect(req, resp, REDIRECT_URL);
        }
    }

    @Nested
    class TestsWitnPostSavedRequest {

        @BeforeEach
        void init() {
            ReflectionTestUtils.setField(successHandler, WEBCERT_DOMAIN_NAME, WEBCERT_DOMAIN_NAME_VALUE);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            when(requestCache.getRequest(req, resp)).thenReturn(savedRequest);
            when(savedRequest.getMethod()).thenReturn(POST);
            when(savedRequest.getRedirectUrl()).thenReturn(REDIRECT_URL);
            when(savedRequest.getRequestURI()).thenReturn(REDIRECT_URL);
            when(authentication.getPrincipal()).thenReturn(user);
            successHandler.setRedirectStrategy(redirectStrategy);
        }

        @Test
        void shallRedirectToSavedEndpoint() throws ServletException, IOException {
            when(savedRequest.getParameterMap()).thenReturn(buildParameterMap());
            successHandler.onAuthenticationSuccess(req, resp, authentication);
            verify(redirectStrategy, times(ONE)).sendRedirect(req, resp, REDIRECT_URL + "/saved");
        }

        @Test
        void shallClearAutheticationAttributes() throws ServletException, IOException {
            when(savedRequest.getParameterMap()).thenReturn(buildParameterMap());
            when(req.getSession(false)).thenReturn(session);
            doNothing().when(session).removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            successHandler.onAuthenticationSuccess(req, resp, authentication);
            verify(session, times(ONE)).removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        }

        @Test
        void shallThrowWebCertServiceExceptionIfIntegrationParametersExist() {
            when(user.getParameters()).thenReturn(mock(IntegrationParameters.class));
            assertThrows(WebCertServiceException.class, () -> successHandler.onAuthenticationSuccess(req, resp, authentication));
            verifyNoInteractions(redirectStrategy);
        }

        @Test
        void shallIncludeAlternateSsnIfExists() throws ServletException, IOException {
            when(savedRequest.getParameterMap()).thenReturn(buildParameterMap());
            successHandler.onAuthenticationSuccess(req, resp, authentication);
            verify(user).setParameters(captor.capture());
            assertEquals(PNR, captor.getValue().getAlternateSsn());
        }

        @Test
        void shallIncludeIsPatientDeceasedIfExists() throws ServletException, IOException {
            when(savedRequest.getParameterMap()).thenReturn(buildParameterMap());
            successHandler.onAuthenticationSuccess(req, resp, authentication);
            verify(user).setParameters(captor.capture());
            assertTrue(captor.getValue().isPatientDeceased());
        }

        @Test
        void shallIncludeLaunchIdIfExists() throws ServletException, IOException {
            when(savedRequest.getParameterMap()).thenReturn(buildParameterMap());
            successHandler.onAuthenticationSuccess(req, resp, authentication);
            verify(user).setParameters(captor.capture());
            assertEquals(LAUNCH_ID, captor.getValue().getLaunchId());
        }
    }

    private Map<String, String[]> buildParameterMap() {
        Map<String, String[]> map = new HashMap<>();
        map.put(IntygIntegrationController.PARAM_PATIENT_ALTERNATE_SSN, new String[]{PNR});
        map.put(IntygIntegrationController.PARAM_PATIENT_DECEASED, new String[]{"true"});
        map.put(IntygIntegrationController.PARAM_LAUNCH_ID, new String[]{LAUNCH_ID});
        return map;
    }
}
