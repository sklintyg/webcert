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
package se.inera.intyg.webcert.web.web.controller.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.junit.Test;
import org.junit.jupiter.api.Nested;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@RunWith(MockitoJUnitRunner.class)
public class UserIntegrationControllerTest {

    private static final String GRANTED_ORIGIN = UserOriginType.DJUPINTEGRATION.name();
    private static final String NON_GRANTED_ORIGIN = UserOriginType.NORMAL.name();
    private static final String GRANTED_ROLE = AuthoritiesConstants.ROLE_LAKARE;

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    UserIntegrationController userIntegrationController = new UserIntegrationController();

    @Test
    public void testLogoutNowWithDjupintegration() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        WebCertUser webCertUser = new WebCertUser();
        webCertUser.setOrigin(GRANTED_ORIGIN);
        webCertUser.setRoles(ImmutableMap.of(GRANTED_ROLE, new Role()));

        when(request.getSession()).thenReturn(session);
        when(webCertUserService.getUser()).thenReturn(webCertUser);

        Response res = userIntegrationController.logoutUserNow(request);

        assertEquals(Status.OK.getStatusCode(), res.getStatus());
        verify(webCertUserService).removeSessionNow(session);
    }

    @Test
    public void testLogoutNowWithoutDjupintegration() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        WebCertUser webCertUser = new WebCertUser();
        webCertUser.setOrigin(NON_GRANTED_ORIGIN);
        webCertUser.setRoles(ImmutableMap.of(GRANTED_ROLE, new Role()));

        when(webCertUserService.getUser()).thenReturn(webCertUser);

        Exception e = assertThrows(AuthoritiesException.class,
            () -> userIntegrationController.logoutUserNow(request));

        assertEquals(AuthoritiesException.class, e.getClass());
    }

    @Nested
    public class GrantedRoleTest {

        @Test
        public void shouldReturnLakare() {
            assertEquals(AuthoritiesConstants.ROLE_LAKARE, userIntegrationController.getGrantedRoles()[0]);
        }

        @Test
        public void shouldReturnAdmin() {
            assertEquals(AuthoritiesConstants.ROLE_ADMIN, userIntegrationController.getGrantedRoles()[1]);
        }

        @Test
        public void shouldReturnTandlakare() {
            assertEquals(AuthoritiesConstants.ROLE_TANDLAKARE, userIntegrationController.getGrantedRoles()[2]);
        }

        @Test
        public void shouldReturnBarnmorska() {
            assertEquals(AuthoritiesConstants.ROLE_BARNMORSKA, userIntegrationController.getGrantedRoles()[3]);
        }

        @Test
        public void shouldReturnSjukskoterska() {
            assertEquals(AuthoritiesConstants.ROLE_SJUKSKOTERSKA, userIntegrationController.getGrantedRoles()[4]);
        }
    }
}
