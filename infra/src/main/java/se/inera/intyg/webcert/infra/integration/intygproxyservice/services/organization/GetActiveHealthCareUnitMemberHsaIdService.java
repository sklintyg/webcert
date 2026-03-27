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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareUnitMember;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareUnitMembers;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareUnitMembersRequestDTO;

@Service
@RequiredArgsConstructor
public class GetActiveHealthCareUnitMemberHsaIdService {

  private final GetHealthCareUnitMembersService getHealthCareUnitMembersService;

  public List<String> get(GetHealthCareUnitMembersRequestDTO getHealthCareUnitMembersRequest) {
    final var healthCareUnitMembers =
        getHealthCareUnitMembersService.get(getHealthCareUnitMembersRequest);
    if (responseIsNullOrEmpty(healthCareUnitMembers)) {
      return Collections.emptyList();
    }
    return healthCareUnitMembers.getHealthCareUnitMember().stream()
        .filter(removeInactiveCareUnitMembers())
        .map(HealthCareUnitMember::getHealthCareUnitMemberHsaId)
        .distinct()
        .collect(Collectors.toList());
  }

  private static Predicate<HealthCareUnitMember> removeInactiveCareUnitMembers() {
    return member ->
        isNullOrAfterToday(member.getHealthCareUnitMemberStartDate())
            && isNullOrBeforeToday(member.getHealthCareUnitMemberEndDate());
  }

  private static boolean isNullOrBeforeToday(LocalDateTime date) {
    return date == null || !date.isBefore(LocalDateTime.now(ZoneId.systemDefault()));
  }

  private static boolean isNullOrAfterToday(LocalDateTime date) {
    return date == null || !date.isAfter(LocalDateTime.now(ZoneId.systemDefault()));
  }

  private static boolean responseIsNullOrEmpty(HealthCareUnitMembers healthCareUnitMembers) {
    return healthCareUnitMembers == null
        || healthCareUnitMembers.getHealthCareUnitMember() == null
        || healthCareUnitMembers.getHealthCareUnitMember().isEmpty();
  }
}
