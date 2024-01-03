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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class WebcertLoggingSessionRegistryImplTest {

    @Mock
    private MonitoringLogService monitoringService;

    @Mock
    private FindByIndexNameSessionRepository<?> sessionRepository;

    @InjectMocks
    private WebcertLoggingSessionRegistryImpl loggingSessionRegistry;

    @Test
    void shallLogUserLoginWhenNewSessionIsRegistered() {
        final var sessionId = "session-id";
        final var hsaId = "hsa-id";
        final var authenticationScheme = "authenticationScheme";
        final var origin = "origin";
        final var roles = ImmutableMap.of("LAKARE", new Role());
        final var roleTypeName = "Läkare-AT-typ";
        final var principal = mock(WebCertUser.class);

        when(principal.getHsaId()).thenReturn(hsaId);
        when(principal.getAuthenticationScheme()).thenReturn(authenticationScheme);
        when(principal.getOrigin()).thenReturn(origin);
        when(principal.getRoles()).thenReturn(roles);
        when(principal.getRoleTypeName()).thenReturn(roleTypeName);

        loggingSessionRegistry.registerNewSession(sessionId, principal);

        verify(monitoringService).logUserLogin(hsaId, roles.keySet().iterator().next(), roleTypeName, authenticationScheme, origin);
    }

    @Test
    void shallNotLogAnythingIfPrincipalIsNull() {
        final String sessionId = "session-id-missing-principal";
        loggingSessionRegistry.registerNewSession(sessionId, null);

        verifyNoInteractions(monitoringService);
    }

    @Test
    void shallLogUserLogoutWhenSessionNotExpired() {
        final var sessionId = "session-id";
        final var hsaId = "hsa-id";
        final var authenticationScheme = "authenticationScheme";
        final var principal = mock(WebCertUser.class);

        mockSession(sessionId, principal, false);
        when(principal.getHsaId()).thenReturn(hsaId);
        when(principal.getAuthenticationScheme()).thenReturn(authenticationScheme);

        loggingSessionRegistry.removeSessionInformation(sessionId);

        verify(monitoringService).logUserLogout(hsaId, authenticationScheme);
    }

    @Test
    void shallLogUserSessionExpiredWhenSessionExpired() {
        final var sessionId = "session-id";
        final var hsaId = "hsa-id";
        final var authenticationScheme = "authenticationScheme";
        final var principal = mock(WebCertUser.class);

        mockSession(sessionId, principal, true);
        when(principal.getHsaId()).thenReturn(hsaId);
        when(principal.getAuthenticationScheme()).thenReturn(authenticationScheme);

        loggingSessionRegistry.removeSessionInformation(sessionId);

        verify(monitoringService).logUserSessionExpired(hsaId, authenticationScheme);
    }

    @Test
    void shallNotLogAnythingIfSessionMissing() {
        final var sessionId = "session-id-missing";
        loggingSessionRegistry.removeSessionInformation(sessionId);

        verifyNoInteractions(monitoringService);
    }

    @Test
    void shallNotLogAnythingIfPrincipalMissing() {
        final var sessionId = "session-id-principal-missing";
        mockSession(sessionId, null, false);
        loggingSessionRegistry.removeSessionInformation(sessionId);

        verifyNoInteractions(monitoringService);
    }

    private void mockSession(String sessionId, WebCertUser principal, boolean expired) {
        final var mockedSession = mock(Session.class);
        final var mockedSecurityContext = mock(SecurityContext.class);
        final var mockedAuthentication = mock(Authentication.class);
        doReturn(mockedSession).when(sessionRepository).findById(sessionId);
        doReturn(mockedSecurityContext).when(mockedSession).getAttribute("SPRING_SECURITY_CONTEXT");
        doReturn(mockedAuthentication).when(mockedSecurityContext).getAuthentication();
        if (principal != null) {
            doReturn(principal).when(mockedAuthentication).getPrincipal();
            doReturn(expired).when(mockedSession).isExpired();
        }
    }
}
