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
package se.inera.intyg.webcert.web.service.employee;

import jakarta.xml.ws.WebServiceException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.webcert.infra.integration.hsatk.services.legacy.HsaEmployeeService;

@Service
@RequiredArgsConstructor
public class EmployeeNameService {

  private final HsaEmployeeService hsaEmployeeService;

  public String getEmployeeHsaName(String employeeHsaId) {
    final var employeeInfo = getEmployee(employeeHsaId);
    if (isEmpty(employeeInfo)) {
      return employeeHsaId;
    }

    return getName(employeeInfo).orElse(employeeHsaId);
  }

  private List<PersonInformation> getEmployee(String employeeHsaId) {
    try {
      return hsaEmployeeService.getEmployee(employeeHsaId, null, null);
    } catch (WebServiceException e) {
      return Collections.emptyList();
    }
  }

  private boolean isEmpty(List<PersonInformation> employeeInfo) {
    return employeeInfo == null || employeeInfo.isEmpty();
  }

  private Optional<String> getName(List<PersonInformation> employeeInfo) {
    return employeeInfo.stream()
        .filter(info -> isDefined(info.getMiddleAndSurName()))
        .map(this::buildName)
        .findFirst();
  }

  private String buildName(PersonInformation personInformation) {
    return isDefined(personInformation.getGivenName())
        ? "%s %s"
            .formatted(personInformation.getGivenName(), personInformation.getMiddleAndSurName())
        : personInformation.getMiddleAndSurName();
  }

  private boolean isDefined(String value) {
    return value != null && !value.isBlank();
  }
}
