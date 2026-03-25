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

import jakarta.xml.ws.WebServiceException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Commission;
import se.inera.intyg.webcert.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.webcert.infra.integration.hsatk.services.legacy.HsaPersonService;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.employee.GetEmployeeRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.employee.GetEmployeeService;

@Slf4j
@Service
@RequiredArgsConstructor
public class HsaLegacyIntegrationPersonService implements HsaPersonService {

  private final GetEmployeeService getEmployeeService;

  @Override
  public List<PersonInformation> getHsaPersonInfo(String personHsaId) {
    try {
      return getEmployeeService.get(GetEmployeeRequestDTO.builder().hsaId(personHsaId).build());
    } catch (Exception e) {
      throw new WebServiceException(e.getMessage());
    }
  }

  @Override
  public List<Commission> checkIfPersonHasMIUsOnUnit(String hosPersonHsaId, String unitHsaId) {
    return null;
  }
}
