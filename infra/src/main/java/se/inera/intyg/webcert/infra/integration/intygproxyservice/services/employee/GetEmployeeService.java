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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.employee;

import static se.inera.intyg.webcert.infra.integration.intygproxyservice.constants.HsaIntygProxyServiceConstants.EMPLOYEE_CACHE_NAME;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.client.employee.HsaIntygProxyServiceEmployeeClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.employee.GetEmployeeRequestDTO;

@Service
@RequiredArgsConstructor
public class GetEmployeeService {

  private final HsaIntygProxyServiceEmployeeClient hsaIntygProxyServiceEmployeeClient;

  @Cacheable(
      cacheNames = EMPLOYEE_CACHE_NAME,
      key = "#getEmployeeRequestDTO.personId + #getEmployeeRequestDTO.hsaId",
      unless = "#result == null")
  public List<PersonInformation> get(GetEmployeeRequestDTO getEmployeeRequestDTO) {
    validateRequestParameters(getEmployeeRequestDTO);
    final var employee = hsaIntygProxyServiceEmployeeClient.getEmployee(getEmployeeRequestDTO);
    return employee.getEmployee().getPersonInformation();
  }

  private void validateRequestParameters(GetEmployeeRequestDTO getEmployeeRequestDTO) {
    if (isNullOrEmpty(getEmployeeRequestDTO.getHsaId())
        && isNullOrEmpty(getEmployeeRequestDTO.getPersonId())) {
      throw new IllegalArgumentException(
          "Missing required parameters. Must provide either personalIdentityNumber or personHsaId");
    }
    if (!isNullOrEmpty(getEmployeeRequestDTO.getHsaId())
        && !isNullOrEmpty(getEmployeeRequestDTO.getPersonId())) {
      throw new IllegalArgumentException(
          "Only provide either personalIdentityNumber or personHsaId. ");
    }
  }

  private boolean isNullOrEmpty(String value) {
    return value == null || value.isEmpty();
  }
}
