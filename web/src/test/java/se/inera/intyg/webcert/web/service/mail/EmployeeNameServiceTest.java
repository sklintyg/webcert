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
package se.inera.intyg.webcert.web.service.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import jakarta.xml.ws.WebServiceException;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.webcert.infra.integration.hsatk.services.legacy.HsaEmployeeService;
import se.inera.intyg.webcert.web.service.employee.EmployeeNameService;

@ExtendWith(MockitoExtension.class)
class EmployeeNameServiceTest {

  @Mock private HsaEmployeeService hsaEmployeeService;

  @InjectMocks private EmployeeNameService employeeNameService;

  @Test
  void shallReturnNameIfEmployeeExists() {
    final var personInformation = new PersonInformation();
    personInformation.setGivenName("givenName");
    personInformation.setMiddleAndSurName("middleAnd surName");

    doReturn(Collections.singletonList(personInformation))
        .when(hsaEmployeeService)
        .getEmployee(any(), any(), any());

    final var actualName = employeeNameService.getEmployeeHsaName("employeeId");

    assertEquals(actualName, "givenName middleAnd surName");
  }

  @Test
  void shallReturnHsaIdAsNameIfEmployeeEmpty() {
    doReturn(Collections.emptyList()).when(hsaEmployeeService).getEmployee(any(), any(), any());

    final var actualName = employeeNameService.getEmployeeHsaName("employeeId");

    assertEquals(actualName, "employeeId");
  }

  @Test
  void shallReturnHsaIdAsNameIfEmployeeDoesNotHaveMiddleAndSurname() {
    final var personInformation = new PersonInformation();
    personInformation.setGivenName("givenName");
    doReturn(Collections.singletonList(personInformation))
        .when(hsaEmployeeService)
        .getEmployee(any(), any(), any());

    final var actualName = employeeNameService.getEmployeeHsaName("employeeId");

    assertEquals(actualName, "employeeId");
  }

  @Test
  void shallReturnMiddleAndSurnameIfEmployeeDoesNotHaveGivenName() {
    final var personInformation = new PersonInformation();
    personInformation.setMiddleAndSurName("middleAnd surName");
    doReturn(Collections.singletonList(personInformation))
        .when(hsaEmployeeService)
        .getEmployee(any(), any(), any());

    final var actualName = employeeNameService.getEmployeeHsaName("employeeId");

    assertEquals(actualName, "middleAnd surName");
  }

  @Test
  void shallReturnHsaIdAsNameIfExceptionIsThrown() {
    doThrow(new WebServiceException("Something went wrong"))
        .when(hsaEmployeeService)
        .getEmployee(any(), any(), any());

    final var actualName = employeeNameService.getEmployeeHsaName("employeeId");

    assertEquals(actualName, "employeeId");
  }
}
