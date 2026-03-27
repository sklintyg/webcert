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

import static se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.OrganizationUtil.isActive;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Commission;
import se.inera.intyg.webcert.infra.integration.hsatk.model.CredentialInformation;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.UserAuthorizationInfo;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.converter.CommissionNameMapConverter;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.converter.UserCredentialListConverter;

@Service
@RequiredArgsConstructor
public class GetUserAuthorizationInfoService {

  private static final String VARD_OCH_BEHANDLING = "Vård och behandling";

  private final GetCareProviderListService getCareProviderListService;
  private final UserCredentialListConverter userCredentialListConverter;
  private final CommissionNameMapConverter commissionNameMapConverter;

  public UserAuthorizationInfo get(List<CredentialInformation> credentialInformation) {
    final var commissionList =
        credentialInformation.stream()
            .flatMap(information -> information.getCommission().stream())
            .filter(GetUserAuthorizationInfoService::isCommissionActive)
            .filter(GetUserAuthorizationInfoService::hasCorrectPurpose)
            .collect(Collectors.toList());

    final var userCredentials = userCredentialListConverter.convert(credentialInformation);
    final var commissionNameMap = commissionNameMapConverter.convert(commissionList);
    final var careProviderList = getCareProviderListService.get(commissionList);
    return new UserAuthorizationInfo(userCredentials, careProviderList, commissionNameMap);
  }

  private static boolean isCommissionActive(Commission commission) {
    return isActive(
        commission.getHealthCareProviderStartDate(), commission.getHealthCareProviderEndDate());
  }

  private static boolean hasCorrectPurpose(Commission commission) {
    return VARD_OCH_BEHANDLING.equalsIgnoreCase(commission.getCommissionPurpose());
  }
}
