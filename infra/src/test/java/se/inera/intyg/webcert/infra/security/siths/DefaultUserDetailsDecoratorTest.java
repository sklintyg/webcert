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
package se.inera.intyg.webcert.infra.security.siths;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import se.inera.intyg.infra.integration.hsatk.model.HsaSystemRole;
import se.inera.intyg.infra.integration.hsatk.model.legacy.UserCredentials;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.AuthConstants;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.IntygUser;

/** Created by eriklupander on 2016-05-19. */
class DefaultUserDetailsDecoratorTest {

  private static final String HSA_ID = "hsa-123";
  private DefaultUserDetailsDecorator testee = new DefaultUserDetailsDecorator();

  @Test
  void testDecorate() {
    IntygUser intygUser = new IntygUser(HSA_ID);
    testee.decorateIntygUserWithAuthenticationMethod(
        intygUser, AuthConstants.FAKE_AUTHENTICATION_SITHS_CONTEXT_REF);
    assertEquals(AuthenticationMethod.FAKE, intygUser.getAuthenticationMethod());
  }

  @Test
  void testSetFirstVardenhetOnFirstVardgivareAsDefault() {
    // Arrange
    Vardgivare vardgivare = new Vardgivare("vg-1", "IFV Testlandsting");
    Vardenhet enhet1 = new Vardenhet("ve-1", "VårdEnhet2A");
    vardgivare.getVardenheter().add(enhet1);
    Vardenhet enhet2 = new Vardenhet("ve-2", "Vårdcentralen");
    vardgivare.getVardenheter().add(enhet2);

    IntygUser user = new IntygUser(HSA_ID);
    user.setVardgivare(Collections.singletonList(vardgivare));

    // Test
    testee.decorateIntygUserWithDefaultVardenhet(user);

    // Verify
    assertEquals(vardgivare, user.getValdVardgivare());
    assertEquals(enhet1, user.getValdVardenhet());
  }

  @Test
  void testDecorateIntygUserWithDefaultVardenhetEmptyVardgivare() {
    Vardgivare vardgivareWithoutEnhet = new Vardgivare("vg-1", "Tom vardgivare");

    Vardgivare vardgivare = new Vardgivare("vg-2", "IFV Testlandsting");
    Vardenhet enhet1 = new Vardenhet("ve-1", "VårdEnhet2A");
    vardgivare.getVardenheter().add(enhet1);
    Vardenhet enhet2 = new Vardenhet("ve-2", "Vårdcentralen");
    vardgivare.getVardenheter().add(enhet2);

    IntygUser user = new IntygUser(HSA_ID);
    user.setVardgivare(Arrays.asList(vardgivareWithoutEnhet, vardgivare));

    testee.decorateIntygUserWithDefaultVardenhet(user);

    assertEquals(vardgivare, user.getValdVardgivare());
    assertEquals(enhet1, user.getValdVardenhet());
  }

  @Test
  void testRehabSystemRoleInRoleOnly() {
    IntygUser user = new IntygUser(HSA_ID);
    UserCredentials userCredentials = new UserCredentials();
    userCredentials.getHsaSystemRole().add(hsaSystemRole(null, "INTYG;Rehab-1234"));

    testee.decorateIntygUserWithSystemRoles(user, userCredentials);
    assertEquals("INTYG;Rehab-1234", user.getSystemRoles().get(0));
  }

  @Test
  void testRehabSystemRoleInRoleOnlyWithSpacedStringAsSystemId() {
    IntygUser user = new IntygUser(HSA_ID);
    UserCredentials userCredentials = new UserCredentials();
    userCredentials.getHsaSystemRole().add(hsaSystemRole(" ", "INTYG;Rehab-1234"));

    testee.decorateIntygUserWithSystemRoles(user, userCredentials);
    assertEquals("INTYG;Rehab-1234", user.getSystemRoles().get(0));
  }

  @Test
  void testRehabSystemRoleFromSystemAndRole() {
    IntygUser user = new IntygUser(HSA_ID);
    UserCredentials userCredentials = new UserCredentials();
    userCredentials.getHsaSystemRole().add(hsaSystemRole("INTYG", "Rehab-1234"));

    testee.decorateIntygUserWithSystemRoles(user, userCredentials);
    assertEquals("INTYG;Rehab-1234", user.getSystemRoles().get(0));
  }

  private HsaSystemRole hsaSystemRole(String systemId, String role) {
    HsaSystemRole hsaSystemRole = new HsaSystemRole();
    hsaSystemRole.setSystemId(systemId);
    hsaSystemRole.setRole(role);
    return hsaSystemRole;
  }
}
