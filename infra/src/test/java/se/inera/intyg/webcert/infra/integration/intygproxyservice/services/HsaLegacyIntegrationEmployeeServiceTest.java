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

import jakarta.xml.ws.WebServiceException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.employee.GetEmployeeRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.employee.GetEmployeeService;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.employee.HsaLegacyIntegrationEmployeeService;

@ExtendWith(MockitoExtension.class)
class HsaLegacyIntegrationEmployeeServiceTest {

  private static final List<PersonInformation> EXPECTED_RESULT = List.of(new PersonInformation());
  @Mock private GetEmployeeService getEmployeeService;

  @InjectMocks private HsaLegacyIntegrationEmployeeService integrationEmployeeService;

  private static final String HSA_ID = "hsaId";
  private static final GetEmployeeRequestDTO GET_EMPLOYEE_REQUEST_DTO =
      GetEmployeeRequestDTO.builder().hsaId(HSA_ID).build();

  @Test
  void shouldThrowWebServiceException() {
    when(getEmployeeService.get(GET_EMPLOYEE_REQUEST_DTO)).thenReturn(Collections.emptyList());

    assertThrows(
        WebServiceException.class, () -> integrationEmployeeService.getEmployee(HSA_ID, null));
    assertThrows(
        WebServiceException.class,
        () -> integrationEmployeeService.getEmployee(HSA_ID, null, null));
  }

  @Test
  void shouldReturnListOfPersonInformation() {
    when(getEmployeeService.get(GET_EMPLOYEE_REQUEST_DTO)).thenReturn(EXPECTED_RESULT);

    final var result = integrationEmployeeService.getEmployee(HSA_ID, null);

    assertEquals(EXPECTED_RESULT, result);
  }

  @Test
  void shouldReturnListOfPersonInformationWithSearchBase() {
    when(getEmployeeService.get(GET_EMPLOYEE_REQUEST_DTO)).thenReturn(EXPECTED_RESULT);

    final var result = integrationEmployeeService.getEmployee(HSA_ID, null, null);

    assertEquals(EXPECTED_RESULT, result);
  }
}
