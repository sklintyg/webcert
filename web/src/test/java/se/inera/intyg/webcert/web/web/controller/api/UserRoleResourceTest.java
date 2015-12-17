/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import se.inera.intyg.webcert.common.common.security.authority.UserRole;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.authtestability.UserRoleResource;

public class UserRoleResourceTest {

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private UserRoleResource userRoleResource;

    @Captor
    private ArgumentCaptor<String[]> roleArrCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetUserRoles() throws Exception {
        //Given
        final WebCertUser user = Mockito.mock(WebCertUser.class);
        final Map<String, UserRole> roleHashMap = new HashMap<>();
        roleHashMap.put(UserRole.ROLE_LAKARE.name(), UserRole.ROLE_LAKARE);
        Mockito.when(user.getRoles()).thenReturn(roleHashMap);
        Mockito.when(webCertUserService.getUser()).thenReturn(user);

        //When
        final Collection<String> rolesResponse = (Collection<String>) userRoleResource.getUserRoles().getEntity();

        //Then
        assertArrayEquals(new String[]{UserRole.ROLE_LAKARE.name()}, rolesResponse.toArray());
    }

    @Test
    public void testSetUserRole() throws Exception {
        //Given
        final WebCertUser user = Mockito.mock(WebCertUser.class);
        Mockito.when(webCertUserService.getUser()).thenReturn(user);

        //When
        final UserRole newRole = UserRole.ROLE_LAKARE_UTHOPP;
        userRoleResource.setUserRole(newRole);

        //Then
        Mockito.verify(webCertUserService, times(1)).updateUserRoles(roleArrCaptor.capture());
        assertEquals(1, roleArrCaptor.getValue().length);
        assertEquals(newRole.name(), roleArrCaptor.getValue()[0]);
    }
}
