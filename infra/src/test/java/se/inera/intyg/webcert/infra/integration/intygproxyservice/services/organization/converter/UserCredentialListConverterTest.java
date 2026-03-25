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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.webcert.infra.integration.hsatk.model.CredentialInformation;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HsaSystemRole;

class UserCredentialListConverterTest {

  private static final CredentialInformation CREDENTIAL_1 = new CredentialInformation();
  private static final String P_CODE_1 = "P1";
  private static final String G_CODE_1 = "G1";
  private static final String PA_TITLE_1 = "PA1";
  private static final HsaSystemRole HSA_1 = new HsaSystemRole();
  private static final CredentialInformation CREDENTIAL_2 = new CredentialInformation();
  private static final String P_CODE_2 = "P2";
  private static final String PA_TITLE_2 = "PA2";
  private static final String G_CODE_2 = "G2";
  private static final HsaSystemRole HSA_2 = new HsaSystemRole();
  private static final List<CredentialInformation> CREDENTIALS =
      List.of(CREDENTIAL_1, CREDENTIAL_2);

  private final UserCredentialListConverter converter = new UserCredentialListConverter();

  @BeforeEach
  void setup() {
    CREDENTIAL_1.setPersonalPrescriptionCode(P_CODE_1);
    CREDENTIAL_1.setGroupPrescriptionCode(List.of(G_CODE_1));
    CREDENTIAL_1.setHsaSystemRole(List.of(HSA_1));
    CREDENTIAL_1.setPaTitleCode(List.of(PA_TITLE_1));

    CREDENTIAL_2.setPersonalPrescriptionCode(P_CODE_2);
    CREDENTIAL_2.setGroupPrescriptionCode(List.of(G_CODE_2));
    CREDENTIAL_2.setHsaSystemRole(List.of(HSA_2));
    CREDENTIAL_2.setPaTitleCode(List.of(PA_TITLE_2));
  }

  @Test
  void shouldSetPersonalPrescriptionToNullIfNoFirstCredential() {
    final var response = converter.convert(Collections.emptyList());

    assertNull(response.getPersonalPrescriptionCode());
  }

  @Test
  void shouldSetPersonalPrescriptionCodeOfLastCredential() {
    final var response = converter.convert(CREDENTIALS);

    assertEquals(
        CREDENTIAL_2.getPersonalPrescriptionCode(), response.getPersonalPrescriptionCode());
  }

  @Test
  void shouldSetGroupPrescriptionCodesFromAllCredentials() {
    final var response = converter.convert(CREDENTIALS);

    assertEquals(List.of(G_CODE_1, G_CODE_2), response.getGroupPrescriptionCode());
  }

  @Test
  void shouldSetPaTitleFromAllCredentials() {
    final var response = converter.convert(CREDENTIALS);

    assertEquals(List.of(PA_TITLE_1, PA_TITLE_2), response.getPaTitleCode());
  }

  @Test
  void shouldSetHsaRolesFromAllCredentials() {
    final var response = converter.convert(CREDENTIALS);

    assertEquals(List.of(HSA_1, HSA_2), response.getHsaSystemRole());
  }
}
