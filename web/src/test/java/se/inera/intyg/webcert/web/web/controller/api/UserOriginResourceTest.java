/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.inera.intyg.webcert.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.authtestability.UserResource;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class UserOriginResourceTest extends AuthoritiesConfigurationTestSetup {

  @Mock private WebCertUserService webCertUserService;

  @InjectMocks private UserResource userResource;

  @Captor private ArgumentCaptor<String> roleArrCaptor;

  @Test
  void testGetUserOrigin() throws Exception {
    String origin = UserOriginType.NORMAL.name();

    // Given
    WebCertUser user = Mockito.mock(WebCertUser.class);

    Mockito.when(user.getOrigin()).thenReturn(origin);
    Mockito.when(webCertUserService.getUser()).thenReturn(user);

    // When
    final String originResponse = (String) userResource.getOrigin().getEntity();

    // Then
    assertEquals(origin, originResponse);
  }

  @Test
  void testSetUserRole() throws Exception {
    // Given
    final WebCertUser user = Mockito.mock(WebCertUser.class);
    Mockito.when(webCertUserService.getUser()).thenReturn(user);

    // When
    String newOrigin = UserOriginType.NORMAL.name();
    userResource.setOrigin(newOrigin);

    // Then
    Mockito.verify(webCertUserService, times(1)).updateOrigin(roleArrCaptor.capture());
    assertEquals(newOrigin, roleArrCaptor.getValue());
  }
}
