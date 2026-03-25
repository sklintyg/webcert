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
package se.inera.intyg.webcert.infra.integration.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.employee.GetEmployeeRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.employee.GetEmployeeService;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.employee.HsaIntegrationEmployeeService;

@ExtendWith(MockitoExtension.class)
class HsaIntegrationEmployeeServiceTest {

  @Mock private GetEmployeeService getEmployeeService;
  @InjectMocks private HsaIntegrationEmployeeService hsaEmployeeService;

  private static final String PERSONAL_IDENTITY_NUMBER = "personalIdentityNumber";
  private static final String PERSON_HSA_ID = "personHsaId";

  @Test
  void shouldReturnEmptyListIfClientThrowsError() {
    when(getEmployeeService.get(
            GetEmployeeRequestDTO.builder()
                .hsaId(PERSON_HSA_ID)
                .personId(PERSONAL_IDENTITY_NUMBER)
                .build()))
        .thenThrow(IllegalStateException.class);
    final var result = hsaEmployeeService.getEmployee(PERSONAL_IDENTITY_NUMBER, PERSON_HSA_ID);

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnListOfPersonInformation() {
    final var expectedResult = List.of(new PersonInformation());
    when(getEmployeeService.get(
            GetEmployeeRequestDTO.builder()
                .hsaId(PERSON_HSA_ID)
                .personId(PERSONAL_IDENTITY_NUMBER)
                .build()))
        .thenReturn(expectedResult);
    final var result = hsaEmployeeService.getEmployee(PERSONAL_IDENTITY_NUMBER, PERSON_HSA_ID);

    assertEquals(expectedResult, result);
  }
}
