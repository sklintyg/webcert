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
package se.inera.intyg.webcert.web.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

class RoleConverterTest {

  private RoleConverter converter = new RoleConverter();

  @BeforeEach
  void setup() {
    cleanup();
  }

  @AfterEach
  void cleanup() {
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  @Test
  void testConvert() {
    String role = "user role";
    WebCertUser user = new WebCertUser();
    user.setRoles(ImmutableMap.of(role, new Role()));
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(user);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    ILoggingEvent event = mock(ILoggingEvent.class);
    String res = converter.convert(event);

    assertEquals(role, res);
  }

  @Test
  void testConvertNoAuth() {
    ILoggingEvent event = mock(ILoggingEvent.class);
    String res = converter.convert(event);

    assertEquals(res, "NO ROLE");
  }

  @Test
  void testConvertAuthNotWebCertUser() {
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn("user");
    SecurityContextHolder.getContext().setAuthentication(authentication);
    ILoggingEvent event = mock(ILoggingEvent.class);
    String res = converter.convert(event);

    assertEquals(res, "NO ROLE");
  }
}
