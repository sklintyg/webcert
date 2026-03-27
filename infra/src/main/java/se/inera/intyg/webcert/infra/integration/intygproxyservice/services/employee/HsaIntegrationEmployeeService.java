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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.employee;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.webcert.infra.integration.hsatk.services.HsatkEmployeeService;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.employee.GetEmployeeRequestDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class HsaIntegrationEmployeeService implements HsatkEmployeeService {

  private final GetEmployeeService getEmployeeService;

  @Override
  public List<PersonInformation> getEmployee(String personalIdentityNumber, String personHsaId) {
    return getEmployee(personalIdentityNumber, personHsaId, null);
  }

  @Override
  public List<PersonInformation> getEmployee(String personId, String personHsaId, String profile) {
    try {
      return getEmployeeService.get(
          GetEmployeeRequestDTO.builder().personId(personId).hsaId(personHsaId).build());
    } catch (Exception exception) {
      log.warn("HsaServiceCallException thrown: {}", exception.getMessage());
      return new ArrayList<>();
    }
  }
}
