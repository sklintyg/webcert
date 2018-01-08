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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@RunWith(MockitoJUnitRunner.class)
public class WebcertLoggingSessionRegistryImplTest {

    @Mock
    private MonitoringLogService monitoringService;

    @InjectMocks
    private WebcertLoggingSessionRegistryImpl loggingSessionRegistry;

    @Test
    public void testRegisterNewSession() throws Exception {
        final String sessionId = "session-id";
        final String hsaId = "hsa-id";
        final String authenticationScheme = "authenticationScheme";
        final String origin = "origin";
        WebCertUser principal = mock(WebCertUser.class);
        when(principal.getHsaId()).thenReturn(hsaId);
        when(principal.getAuthenticationScheme()).thenReturn(authenticationScheme);
        when(principal.getOrigin()).thenReturn(origin);
        loggingSessionRegistry.registerNewSession(sessionId, principal);

        verify(monitoringService).logUserLogin(hsaId, authenticationScheme, origin);
    }

    @Test
    public void testRegisterNewSessionNoWebCertUser() throws Exception {
        final String sessionId = "session-id";
        loggingSessionRegistry.registerNewSession(sessionId, "principal");

        verifyZeroInteractions(monitoringService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterNewSessionNoPrincipal() throws Exception {
        final String sessionId = "session-id";
        try {
            loggingSessionRegistry.registerNewSession(sessionId, null);
        } finally {
            verifyZeroInteractions(monitoringService);
        }
    }

    @Test
    public void testRemoveSessionInformation() throws Exception {
        final String sessionId = "session-id";
        final String hsaId = "hsa-id";
        final String authenticationScheme = "authenticationScheme";
        final String origin = "origin";
        WebCertUser principal = mock(WebCertUser.class);
        when(principal.getHsaId()).thenReturn(hsaId);
        when(principal.getAuthenticationScheme()).thenReturn(authenticationScheme);
        when(principal.getOrigin()).thenReturn(origin);
        loggingSessionRegistry.registerNewSession(sessionId, principal);

        loggingSessionRegistry.removeSessionInformation(sessionId);

        verify(monitoringService).logUserLogout(hsaId, authenticationScheme);
    }

    @Test
    public void testRemoveSessionInformationNoWebCertUser() throws Exception {
        final String sessionId = "session-id";
        loggingSessionRegistry.registerNewSession(sessionId, "principal");

        loggingSessionRegistry.removeSessionInformation(sessionId);

        verifyZeroInteractions(monitoringService);
    }

    @Test
    public void testRemoveSessionInformationNoSession() throws Exception {
        loggingSessionRegistry.removeSessionInformation("session-id");

        verifyZeroInteractions(monitoringService);
    }
}
