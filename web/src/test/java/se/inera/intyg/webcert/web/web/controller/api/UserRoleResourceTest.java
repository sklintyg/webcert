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

package se.inera.intyg.webcert.web.web.controller.api;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.common.security.common.model.Role;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.authtestability.UserResource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

public class UserRoleResourceTest extends AuthoritiesConfigurationTestSetup {

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private UserResource userResource;

    @Captor
    private ArgumentCaptor<String> roleArrCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetUserRoles() throws Exception {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        //Given
        WebCertUser user = Mockito.mock(WebCertUser.class);
        Map<String, Role> roleHashMap = new HashMap<>();
        roleHashMap.put(role.getName(), role);

        Mockito.when(user.getRoles()).thenReturn(roleHashMap);
        Mockito.when(webCertUserService.getUser()).thenReturn(user);

        //When
        final Collection<String> rolesResponse = (Collection<String>) userResource.getUserRoles().getEntity();

        //Then
        assertArrayEquals(new String[]{role.getName()}, rolesResponse.toArray());
    }

    @Test
    public void testSetUserRole() throws Exception {
        //Given
        final WebCertUser user = Mockito.mock(WebCertUser.class);
        Mockito.when(webCertUserService.getUser()).thenReturn(user);

        //When
        //Role newRole = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);
        userResource.setUserRole(AuthoritiesConstants.ROLE_LAKARE);

        //Then
        Mockito.verify(webCertUserService, times(1)).updateUserRole(roleArrCaptor.capture());
        assertEquals(AuthoritiesConstants.ROLE_LAKARE, roleArrCaptor.getValue());
        //assertEquals(1, roleArrCaptor.getValue().length);
        //assertEquals(newRole.getName(), roleArrCaptor.getValue()[0]);
    }
}
