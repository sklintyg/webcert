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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.service.GetPersonIntegrationService;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.service.GetPersonsIntegrationService;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.service.PUIntegrationService;

@ExtendWith(MockitoExtension.class)
class PUIntegrationServiceTest {

  @Mock GetPersonIntegrationService getPersonIntegrationService;
  @Mock GetPersonsIntegrationService getPersonsIntegrationService;
  @InjectMocks PUIntegrationService puIntegrationService;

  @Nested
  class GetPersonTests {

    @Test
    void shallReturnPersonSvar() {
      final var expectedPersonSvar = mock(PersonSvar.class);
      final var personnummer = mock(Personnummer.class);

      doReturn(expectedPersonSvar).when(getPersonIntegrationService).get(personnummer);

      final var actualPersonSvar = puIntegrationService.getPerson(personnummer);
      assertEquals(expectedPersonSvar, actualPersonSvar);
    }
  }

  @Nested
  class GetPersonsTests {

    @Test
    void shallReturnMapOfPersonnummerAndPersonSvar() {
      final var personSvar = mock(PersonSvar.class);
      final var personnummer = mock(Personnummer.class);
      final var expectedResult = Map.of(personnummer, personSvar);

      doReturn(expectedResult).when(getPersonsIntegrationService).get(List.of(personnummer));

      final var actualResult = puIntegrationService.getPersons(List.of(personnummer));
      assertEquals(expectedResult, actualResult);
    }
  }
}
