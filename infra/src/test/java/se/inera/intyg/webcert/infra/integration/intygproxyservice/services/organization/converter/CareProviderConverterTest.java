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

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Commission;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardenhet;

class CareProviderConverterTest {

  private final CareProviderConverter converter = new CareProviderConverter();

  @Test
  void shouldConvertId() {
    final var commission = getCommission();

    final var response = converter.convert(commission, Collections.emptyList());

    assertEquals(commission.getHealthCareProviderHsaId(), response.getId());
  }

  @Test
  void shouldConvertName() {
    final var commission = getCommission();

    final var response = converter.convert(commission, Collections.emptyList());

    assertEquals(commission.getHealthCareProviderName(), response.getNamn());
  }

  @Test
  void shouldConvertVardenheter() {
    final var expected = List.of(new Vardenhet());
    final var commission = getCommission();

    final var response = converter.convert(commission, expected);

    assertEquals(expected, response.getVardenheter());
  }

  private Commission getCommission() {
    final var commission = new Commission();

    commission.setHealthCareProviderHsaId("HSA_ID");

    return commission;
  }
}
