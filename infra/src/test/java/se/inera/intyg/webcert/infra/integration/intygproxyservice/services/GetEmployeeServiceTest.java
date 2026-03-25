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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.exception.HsaServiceCallException;
import se.inera.intyg.webcert.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.client.employee.HsaIntygProxyServiceEmployeeClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.employee.EmployeeDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.employee.GetEmployeeRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.employee.GetEmployeeResponseDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.employee.GetEmployeeService;

@ExtendWith(MockitoExtension.class)
class GetEmployeeServiceTest {

  @Mock private HsaIntygProxyServiceEmployeeClient employeeClient;

  @InjectMocks private GetEmployeeService getEmployeeService;

  private static final String PERSONAL_IDENTITY_NUMBER = "personalIdentityNumber";
  private static final String PERSON_HSA_ID = "personHsaId";

  @Test
  void shouldThrowIfMissingPersonalIdentityNumberAndPersonHsaId() {
    final var request = GetEmployeeRequestDTO.builder().personId(null).hsaId(null).build();
    assertThrows(IllegalArgumentException.class, () -> getEmployeeService.get(request));
  }

  @Test
  void shouldThrowIfBothPersonalIdentityNumberAndPersonHsaIdIsProvided() {
    final var request =
        GetEmployeeRequestDTO.builder()
            .personId(PERSONAL_IDENTITY_NUMBER)
            .hsaId(PERSON_HSA_ID)
            .build();
    assertThrows(IllegalArgumentException.class, () -> getEmployeeService.get(request));
  }

  @Test
  void shouldReturnListOfPersonalInformation() throws HsaServiceCallException {
    final var request = GetEmployeeRequestDTO.builder().personId(PERSONAL_IDENTITY_NUMBER).build();
    final var expectedResult = List.of(new PersonInformation());
    when(employeeClient.getEmployee(request))
        .thenReturn(
            GetEmployeeResponseDTO.builder()
                .employee(EmployeeDTO.builder().personInformation(expectedResult).build())
                .build());
    final var result = getEmployeeService.get(request);
    assertEquals(expectedResult, result);
  }
}
