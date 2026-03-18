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
package se.inera.intyg.webcert.web.privatepractitioner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_HSA_ID;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_NAME;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_PERSON_ID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.facade.GetUserResourceLinks;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class PrivatePractitionerAccessValidationServiceTest {

  @Mock private GetUserResourceLinks getUserResourceLinks;

  @InjectMocks private PrivatePractitionerAccessValidationService accessValidationHelper;

  WebCertUser user;

  @BeforeEach
  void setUp() {
    user = new WebCertUser();
    user.setPersonId(DR_KRANSTEGE_PERSON_ID);
    user.setNamn(DR_KRANSTEGE_NAME);
    user.setHsaId(DR_KRANSTEGE_HSA_ID);
  }

  @Nested
  class UpdateAccessTests {

    @Test
    void shouldReturnTrueWhenPractitionerHasAccessToUpdate() {
      when(getUserResourceLinks.get(user))
          .thenReturn(
              new ResourceLinkDTO[] {
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.ACCESS_EDIT_PRIVATE_PRACTITIONER, "", "", true)
              });
      final var actual = accessValidationHelper.hasAccessToEdit(user);
      assertTrue(actual);
    }

    @Test
    void shouldReturnFalseWhenPractitionerHasNoAccessToUpdate() {
      when(getUserResourceLinks.get(user))
          .thenReturn(
              new ResourceLinkDTO[] {
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.ACCESS_EDIT_PRIVATE_PRACTITIONER, "", "", false)
              });
      final var actual = accessValidationHelper.hasAccessToEdit(user);
      assertFalse(actual);
    }
  }

  @Nested
  class RegisterAccessTests {

    @Test
    void shouldReturnTrueWhenPractitionerHasAccessToRegister() {
      when(getUserResourceLinks.get(user))
          .thenReturn(
              new ResourceLinkDTO[] {
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.ACCESS_REGISTER_PRIVATE_PRACTITIONER, "", "", true)
              });
      final var actual = accessValidationHelper.hasAccessToRegister(user);
      assertTrue(actual);
    }

    @Test
    void shouldReturnFalseWhenPractitionerHasNoAccessToRegister() {
      when(getUserResourceLinks.get(user))
          .thenReturn(
              new ResourceLinkDTO[] {
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.ACCESS_REGISTER_PRIVATE_PRACTITIONER, "", "", false)
              });
      final var actual = accessValidationHelper.hasAccessToRegister(user);
      assertFalse(actual);
    }
  }
}
