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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
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
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.converter.CareProviderConverter;

@ExtendWith(MockitoExtension.class)
class GetCareProviderListServiceTest {

  private static final Vardenhet UNIT_A1 = new Vardenhet();
  private static final Vardenhet UNIT_A2 = new Vardenhet();
  private static final Vardenhet UNIT_B1 = new Vardenhet();

  private static final List<Vardenhet> CARE_UNITS = List.of(UNIT_A1, UNIT_B1, UNIT_A2);

  private static final String CARE_PROVIDER_A_ID = "A_ID";
  private static final String CARE_PROVIDER_B_ID = "B_ID";

  private Vardgivare providerA;
  private Vardgivare providerB;

  private Commission commissionA;
  private Commission commissionB;

  @Mock GetCareUnitListService getCareUnitListService;
  @Mock CareProviderConverter careProviderConverter;

  @InjectMocks GetCareProviderListService getCareProviderListService;

  @Nested
  class CommissionsWithUnits {

    @BeforeEach
    void setup() {
      providerA = new Vardgivare();
      providerB = new Vardgivare();
      providerA.setId(CARE_PROVIDER_A_ID);
      providerB.setId(CARE_PROVIDER_B_ID);
      providerA.setNamn("A_NAME");
      providerB.setNamn("B_NAME");
      providerA.setVardenheter(List.of(UNIT_A1, UNIT_A2));
      providerB.setVardenheter(List.of(UNIT_B1));

      UNIT_A1.setId("ID_A1");
      UNIT_A2.setId("ID_A2");
      UNIT_B1.setId("ID_B1");
      UNIT_A1.setVardgivareHsaId(CARE_PROVIDER_A_ID);
      UNIT_A2.setVardgivareHsaId(CARE_PROVIDER_A_ID);
      UNIT_B1.setVardgivareHsaId(CARE_PROVIDER_B_ID);

      commissionA = new Commission();
      commissionB = new Commission();
      commissionA.setHealthCareProviderHsaId(CARE_PROVIDER_A_ID);
      commissionB.setHealthCareProviderHsaId(CARE_PROVIDER_B_ID);

      when(getCareUnitListService.get(any(List.class))).thenReturn(CARE_UNITS);

      when(careProviderConverter.convert(commissionA, List.of(UNIT_A1, UNIT_A2)))
          .thenReturn(providerA);
      when(careProviderConverter.convert(commissionB, List.of(UNIT_B1))).thenReturn(providerB);
    }

    @Test
    void shouldReturnConvertedCareProvider() {
      final var response = getCareProviderListService.get(List.of(commissionA, commissionB));

      assertEquals(2, response.size());
      assertEquals(providerA, response.get(0));
      assertEquals(providerB, response.get(1));
    }

    @Test
    void shouldFilterDuplicates() {
      final var response =
          getCareProviderListService.get(List.of(commissionA, commissionA, commissionB));

      assertEquals(2, response.size());
    }

    @Test
    void shouldSendCommissionToConverter() {
      final var captor = ArgumentCaptor.forClass(Commission.class);

      getCareProviderListService.get(List.of(commissionA, commissionB));

      verify(careProviderConverter, times(2)).convert(captor.capture(), anyList());
      assertTrue(captor.getAllValues().contains(commissionA));
      assertTrue(captor.getAllValues().contains(commissionB));
    }

    @Test
    void shouldSendUnitListToConverterWhenSeveralUnits() {
      final var captor = ArgumentCaptor.forClass(List.class);

      getCareProviderListService.get(List.of(commissionA, commissionB));

      verify(careProviderConverter, times(2)).convert(any(Commission.class), captor.capture());
      assertTrue(captor.getAllValues().contains(List.of(UNIT_A1, UNIT_A2)));
    }

    @Test
    void shouldSendUnitListToConverter() {
      final var captor = ArgumentCaptor.forClass(List.class);

      getCareProviderListService.get(List.of(commissionA, commissionB));

      verify(careProviderConverter, times(2)).convert(any(Commission.class), captor.capture());
      assertTrue(captor.getAllValues().contains(List.of(UNIT_B1)));
    }

    @Test
    void shouldCallGetCareUnitListServiceOnlyOnce() {
      final var captor = ArgumentCaptor.forClass(List.class);

      getCareProviderListService.get(List.of(commissionA, commissionB));

      verify(getCareUnitListService, times(1)).get(captor.capture());
      assertTrue(captor.getAllValues().contains(List.of(commissionA, commissionB)));
    }
  }

  @Test
  void shouldFilterCareProviderWithoutUnits() {
    final var provider = new Vardgivare();
    provider.setVardenheter(Collections.emptyList());
    final var commissionWithoutUnits = new Commission();
    commissionWithoutUnits.setHealthCareProviderHsaId("ID");

    when(careProviderConverter.convert(any(Commission.class), anyList())).thenReturn(provider);

    final var response = getCareProviderListService.get(List.of(commissionWithoutUnits));

    assertTrue(response.isEmpty());
  }
}
