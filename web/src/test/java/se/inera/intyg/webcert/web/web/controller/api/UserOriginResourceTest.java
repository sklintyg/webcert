/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
import org.mockito.*;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.authtestability.UserResource;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

public class UserOriginResourceTest extends AuthoritiesConfigurationTestSetup {

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
    public void testGetUserOrigin() throws Exception {
        String origin = UserOriginType.NORMAL.name();

        //Given
        WebCertUser user = Mockito.mock(WebCertUser.class);

        Mockito.when(user.getOrigin()).thenReturn(origin);
        Mockito.when(webCertUserService.getUser()).thenReturn(user);

        //When
        final String originResponse = (String) userResource.getOrigin().getEntity();

        //Then
        assertEquals(origin, originResponse);
    }

    @Test
    public void testSetUserRole() throws Exception {
        //Given
        final WebCertUser user = Mockito.mock(WebCertUser.class);
        Mockito.when(webCertUserService.getUser()).thenReturn(user);

        //When
        String newOrigin = UserOriginType.UTHOPP.name();
        userResource.setOrigin(newOrigin);

        //Then
        Mockito.verify(webCertUserService, times(1)).updateOrigin(roleArrCaptor.capture());
        assertEquals(newOrigin, roleArrCaptor.getValue());
    }
}
