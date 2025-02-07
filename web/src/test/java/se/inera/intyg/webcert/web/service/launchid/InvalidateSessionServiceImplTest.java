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
package se.inera.intyg.webcert.web.service.launchid;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.MapSession;
import org.springframework.session.Session;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.InvalidateRequest;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
class InvalidateSessionServiceImplTest {

    private static final String LAUNCH_ID = "97f279ba-7d2b-4b0a-8665-7adde08f26f4";
    private static final String LAUNCH_ID_WRONG = "97f279ba-7d2b-4b0a-8665-7adde08f26f5";
    private static final String USER_HSA_ID = "TSTNMT2321000156-1079";
    private static final String USER_HSA_ID_WRONG = "TSTNMT2321000156-1078";
    private static final String ENCODED_SESSION_ID = "OTdmMjc5YmEtN2QyYi00YjBhLTg2NjUtN2FkZGUwOGYyNmY0";
    @Mock
    private Cache redisCacheLaunchId;
    private FindByIndexNameSessionRepository<Session> sessionRepository = mock(FindByIndexNameSessionRepository.class,
        withSettings().lenient());
    @InjectMocks
    public InvalidateSessionServiceImpl launchIdService;
    private InvalidateRequest invalidateRequest;

    @BeforeEach
    public void setup() {
        invalidateRequest = getInvalidateRequest();
        when(redisCacheLaunchId.get(invalidateRequest.getLaunchId(), String.class)).thenReturn(ENCODED_SESSION_ID);
    }

    @Test
    void sessionShouldBeRemovedWhenValuesMatch() {
        WebCertUser userWithCorrectValues = createUserWithMatchingLaunchIdAndHsaId();
        Session session = createSession(ENCODED_SESSION_ID, userWithCorrectValues);

        when(sessionRepository.findById(LAUNCH_ID)).thenReturn(session);

        launchIdService.invalidateSessionIfActive(getInvalidateRequest());

        verify(sessionRepository).deleteById(anyString());
    }

    @Test
    void sessionShouldNotBeRemovedWhenHsaIdDoesNotMatch() {
        WebCertUser userWithWrongHsaId = createUserWithMatchingLaunchIdButWrongHsaId();

        Session session = createSession(ENCODED_SESSION_ID, userWithWrongHsaId);

        when(sessionRepository.findById(LAUNCH_ID)).thenReturn(session);

        launchIdService.invalidateSessionIfActive(getInvalidateRequest());

        verify(sessionRepository, never()).deleteById(anyString());
    }

    @Test
    void sessionShouldNotBeRemovedWhenLaunchIdIsNotPresentInRedis() {
        WebCertUser userWithWrongHsaId = createUserWithMatchingLaunchIdButWrongHsaId();
        Session session = createSession(ENCODED_SESSION_ID, userWithWrongHsaId);

        when(sessionRepository.findById(LAUNCH_ID)).thenReturn(session);
        when(redisCacheLaunchId.get(invalidateRequest.getLaunchId(), String.class)).thenReturn(null);

        launchIdService.invalidateSessionIfActive(getInvalidateRequest());

        verify(sessionRepository, never()).deleteById(anyString());
    }

    @Test
    void sessionShouldNotBeRemovedWhenSessionIsNotPresentInRedis() {
        WebCertUser userWithWrongHsaId = createUserWithMatchingLaunchIdButWrongHsaId();
        Session session = createSession(ENCODED_SESSION_ID, userWithWrongHsaId);

        when(sessionRepository.findById(LAUNCH_ID)).thenReturn(null);
        when(redisCacheLaunchId.get(invalidateRequest.getLaunchId(), String.class)).thenReturn(ENCODED_SESSION_ID);

        launchIdService.invalidateSessionIfActive(getInvalidateRequest());

        verify(sessionRepository, never()).deleteById(anyString());
    }

    private InvalidateRequest getInvalidateRequest() {
        InvalidateRequest dto = new InvalidateRequest();
        dto.setLaunchId(LAUNCH_ID);
        dto.setUserHsaId(USER_HSA_ID);
        return dto;
    }

    private WebCertUser createUserWithMatchingLaunchIdAndHsaId() {
        WebCertUser user = new WebCertUser();
        user.setParameters(new IntegrationParameters(null, null, "", null, null, null, null,
            null, null, false, false, false, false, LAUNCH_ID));
        user.setHsaId(USER_HSA_ID);
        return user;
    }

    private WebCertUser createUserWithMatchingHsaIdButWrongLaunchId() {
        WebCertUser user = new WebCertUser();
        user.setParameters(new IntegrationParameters(null, null, "", null, null, null, null,
            null, null, false, false, false, false, LAUNCH_ID_WRONG));
        user.setHsaId(USER_HSA_ID);
        return user;
    }

    private WebCertUser createUserWithMatchingLaunchIdButWrongHsaId() {
        WebCertUser user = new WebCertUser();
        user.setParameters(new IntegrationParameters(null, null, "", null, null, null, null,
            null, null, false, false, false, false, LAUNCH_ID));
        user.setHsaId(USER_HSA_ID_WRONG);
        return user;
    }

    private Session createSession(String sessionId, WebCertUser user) {
        MapSession session = new MapSession(sessionId);
        Authentication authentication = mock(Authentication.class, withSettings().lenient());
        given(authentication.getPrincipal()).willReturn(user);
        SecurityContextImpl securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authentication);
        session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
        return session;
    }
}
