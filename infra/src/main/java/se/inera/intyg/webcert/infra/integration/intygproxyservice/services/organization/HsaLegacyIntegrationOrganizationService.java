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

import jakarta.xml.ws.WebServiceException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.integration.hsatk.exception.HsaServiceCallException;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.UserAuthorizationInfo;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.authorization.GetCredentialInformationRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareUnitMembersRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareUnitRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetUnitRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.authorization.GetCredentialInformationForPersonService;

@Slf4j
@Service
@RequiredArgsConstructor
public class HsaLegacyIntegrationOrganizationService implements HsaOrganizationsService {

  private final GetActiveHealthCareUnitMemberHsaIdService getActiveHealthCareUnitMemberHsaIdService;
  private final GetHealthCareUnitService getHealthCareUnitService;
  private final GetUnitService getUnitService;
  private final GetCredentialInformationForPersonService getCredentialInformationForPersonService;
  private final GetUserAuthorizationInfoService getUserAuthorizationInfoService;
  private final GetCareUnitService getCareUnitService;

  @Override
  public UserAuthorizationInfo getAuthorizedEnheterForHosPerson(String hosPersonHsaId) {
    final var credentialInformation =
        getCredentialInformationForPersonService.get(
            GetCredentialInformationRequestDTO.builder().personHsaId(hosPersonHsaId).build());

    return getUserAuthorizationInfoService.get(credentialInformation);
  }

  @Override
  public String getVardgivareOfVardenhet(String vardenhetHsaId) {
    final var healthCareUnit =
        getHealthCareUnitService.get(
            GetHealthCareUnitRequestDTO.builder().hsaId(vardenhetHsaId).build());
    return healthCareUnit.getHealthCareProviderHsaId();
  }

  @Override
  public Vardenhet getVardenhet(String vardenhetHsaId) {
    return getCareUnitService.get(vardenhetHsaId);
  }

  @Override
  public Vardgivare getVardgivareInfo(String vardgivareHsaId) {
    final var unit = getUnitService.get(GetUnitRequestDTO.builder().hsaId(vardgivareHsaId).build());

    if (unit == null) {
      throw new WebServiceException("Could not get unit for unitHsaId " + vardgivareHsaId);
    }

    return new Vardgivare(unit.getUnitHsaId(), unit.getUnitName());
  }

  @Override
  public List<String> getHsaIdForAktivaUnderenheter(String vardEnhetHsaId) {
    return getActiveHealthCareUnitMemberHsaIdService.get(
        GetHealthCareUnitMembersRequestDTO.builder().hsaId(vardEnhetHsaId).build());
  }

  @Override
  public String getParentUnit(String hsaId) throws HsaServiceCallException {
    try {
      final var unit =
          getHealthCareUnitService.get(GetHealthCareUnitRequestDTO.builder().hsaId(hsaId).build());

      if (unit == null) {
        throw new HsaServiceCallException(
            String.format("Unable to find unit with hsaId '%s'", hsaId));
      }
      return unit.getHealthCareUnitHsaId();
    } catch (Exception exception) {
      throw new HsaServiceCallException(exception);
    }
  }
}
