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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization;

import static se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.OrganizationUtil.isActive;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Commission;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardenhet;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetCareUnitListService {

  private final GetCareUnitService getCareUnitService;

  public List<Vardenhet> get(List<Commission> commissions) {
    return commissions.stream()
        .filter(this::isUnitActive)
        .map(getCareUnitService::get)
        .filter(Objects::nonNull)
        .distinct()
        .sorted(Comparator.comparing(Vardenhet::getNamn))
        .collect(Collectors.toList());
  }

  private boolean isUnitActive(Commission commission) {
    return isActive(commission.getHealthCareUnitStartDate(), commission.getHealthCareUnitEndDate());
  }
}
