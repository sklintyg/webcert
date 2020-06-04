/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.Test;
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

    private static final String LOGOUT_NOW_COMPATIBLE_ORIGIN = UserOriginType.DJUPINTEGRATION.name();
    private static final String LOGOUT_NOW_NON_COMPATIBLE_ORIGIN = UserOriginType.NORMAL.name();
    private static final String LOGOUT_NOW_COMPATIBLE_ROLE = AuthoritiesConstants.ROLE_LAKARE;

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    UserIntegrationController userIntegrationController = new UserIntegrationController();

    @Test
    public void testLogoutNowWithDjupintegration() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        WebCertUser webCertUser = new WebCertUser();
        webCertUser.setOrigin(LOGOUT_NOW_COMPATIBLE_ORIGIN);
        webCertUser.setRoles(ImmutableMap.of(LOGOUT_NOW_COMPATIBLE_ROLE, new Role()));

        when(request.getSession()).thenReturn(session);
        when(webCertUserService.getUser()).thenReturn(webCertUser);

        userIntegrationController.logoutUserNow(request);

        verify(webCertUserService).removeSessionNow(session);
    }

    @Test
    public void testLogoutNowWithoutDjupintegration() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        WebCertUser webCertUser = new WebCertUser();
        webCertUser.setOrigin(LOGOUT_NOW_NON_COMPATIBLE_ORIGIN);
        webCertUser.setRoles(ImmutableMap.of(LOGOUT_NOW_COMPATIBLE_ROLE, new Role()));

        when(webCertUserService.getUser()).thenReturn(webCertUser);

        Exception e = assertThrows(AuthoritiesException.class,
            () -> userIntegrationController.logoutUserNow(request));

        assertEquals(AuthoritiesException.class, e.getClass());
    }
}
