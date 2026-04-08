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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Commission;
import se.inera.intyg.webcert.infra.integration.hsatk.model.CredentialInformation;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.UserCredentials;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.converter.CommissionNameMapConverter;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.converter.UserCredentialListConverter;

@ExtendWith(MockitoExtension.class)
class GetUserAuthorizationInfoServiceTest {

  @Mock private GetCareProviderListService getCareProviderListService;

  @Mock private UserCredentialListConverter userCredentialListConverter;

  @Mock private CommissionNameMapConverter commissionNameMapConverter;

  @InjectMocks GetUserAuthorizationInfoService getUserAuthorizationInfoService;

  private Commission active;
  private List<CredentialInformation> credentials;

  @BeforeEach
  void setup() {
    credentials = new ArrayList<>();
    active = new Commission();
    final var inactive = new Commission();
    final var notCare = new Commission();
    inactive.setHealthCareProviderStartDate(LocalDateTime.now().plusDays(1));
    active.setCommissionPurpose("Vård och behandling");
    inactive.setCommissionPurpose("Vård och behandling");
    notCare.setCommissionPurpose("NOT_IT");

    final var withActive = new CredentialInformation();
    final var withInactive = new CredentialInformation();

    withActive.setCommission(List.of(active, notCare));
    withInactive.setCommission(List.of(inactive));

    credentials.add(withActive);
    credentials.add(withInactive);
  }

  @Nested
  class UserCredentialsTest {

    @Test
    void shouldSendCredentials() {
      final var captor = ArgumentCaptor.forClass(List.class);

      getUserAuthorizationInfoService.get(credentials);
      verify(userCredentialListConverter).convert(captor.capture());

      assertEquals(credentials, captor.getValue());
    }

    @Test
    void shouldReturnValueInUserAuthorization() {
      final var expected = new UserCredentials();
      when(userCredentialListConverter.convert(anyList())).thenReturn(expected);

      final var response = getUserAuthorizationInfoService.get(credentials);

      assertEquals(expected, response.getUserCredentials());
    }
  }

  @Nested
  class CareProviderList {

    @Test
    void shouldFilterCommissions() {
      final var captor = ArgumentCaptor.forClass(List.class);

      getUserAuthorizationInfoService.get(credentials);
      verify(getCareProviderListService).get(captor.capture());

      assertEquals(1, captor.getValue().size());
      assertEquals(active, captor.getValue().get(0));
    }

    @Test
    void shouldReturnValueInUserAuthorization() {
      final var expected = List.of(new Vardgivare());
      when(getCareProviderListService.get(anyList())).thenReturn(expected);

      final var response = getUserAuthorizationInfoService.get(credentials);

      assertEquals(expected, response.getVardgivare());
    }
  }

  @Nested
  class CommissionNameMap {

    @Test
    void shouldFilterInactiveCommissions() {
      final var captor = ArgumentCaptor.forClass(List.class);

      getUserAuthorizationInfoService.get(credentials);
      verify(commissionNameMapConverter).convert(captor.capture());

      assertEquals(1, captor.getValue().size());
      assertEquals(active, captor.getValue().get(0));
    }

    @Test
    void shouldReturnValueInUserAuthorization() {
      final var expected = new HashMap<String, String>();
      when(commissionNameMapConverter.convert(anyList())).thenReturn(expected);

      final var response = getUserAuthorizationInfoService.get(credentials);

      assertEquals(expected, response.getCommissionNamePerCareUnit());
    }
  }
}
