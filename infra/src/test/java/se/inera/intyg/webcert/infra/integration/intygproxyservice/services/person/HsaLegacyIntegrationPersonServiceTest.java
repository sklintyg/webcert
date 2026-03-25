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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.xml.ws.WebServiceException;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.employee.GetEmployeeRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.employee.GetEmployeeService;

@ExtendWith(MockitoExtension.class)
class HsaLegacyIntegrationPersonServiceTest {

  @Mock GetEmployeeService getEmployeeService;

  @InjectMocks HsaLegacyIntegrationPersonService hsaLegacyIntegrationPersonService;

  @Test
  void shouldThrowError() {
    when(getEmployeeService.get(any(GetEmployeeRequestDTO.class)))
        .thenThrow(IllegalArgumentException.class);

    assertThrows(
        WebServiceException.class, () -> hsaLegacyIntegrationPersonService.getHsaPersonInfo("id"));
  }

  @Test
  void shouldReturnPersonInformation() {
    final var expected = Collections.singletonList(new PersonInformation());
    when(getEmployeeService.get(any(GetEmployeeRequestDTO.class))).thenReturn(expected);

    final var response = hsaLegacyIntegrationPersonService.getHsaPersonInfo("id");

    assertEquals(expected, response);
  }
}
