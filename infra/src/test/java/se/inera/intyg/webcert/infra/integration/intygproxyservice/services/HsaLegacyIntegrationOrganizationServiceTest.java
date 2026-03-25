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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.xml.ws.WebServiceException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.exception.HsaServiceCallException;
import se.inera.intyg.webcert.infra.integration.hsatk.model.CredentialInformation;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareUnit;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Unit;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.UserAuthorizationInfo;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.authorization.GetCredentialInformationRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareUnitMembersRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareUnitRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetUnitRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.authorization.GetCredentialInformationForPersonService;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.GetActiveHealthCareUnitMemberHsaIdService;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.GetCareUnitService;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.GetHealthCareUnitService;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.GetUnitService;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.GetUserAuthorizationInfoService;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.services.organization.HsaLegacyIntegrationOrganizationService;

@ExtendWith(MockitoExtension.class)
class HsaLegacyIntegrationOrganizationServiceTest {

  public static final String CARE_PROVIDER_HSA_ID = "careProviderHsaId";
  private static final String CARE_UNIT_HSA_ID = "careUnitHsaId";

  @Mock private GetHealthCareUnitService getHealthCareUnitService;

  @Mock private GetActiveHealthCareUnitMemberHsaIdService getHealthCareUnitMemberHsaIdService;

  @Mock private GetUnitService getUnitService;

  @Mock private GetCareUnitService getCareUnitService;

  @Mock private GetCredentialInformationForPersonService getCredentialInformationForPersonService;

  @Mock private GetUserAuthorizationInfoService getUserAuthorizationInfoService;

  @InjectMocks
  private HsaLegacyIntegrationOrganizationService hsaLegacyIntegrationOrganizationService;

  @Nested
  class VardgivareOfvardenhet {

    @Test
    void shouldReturnHealthCareProviderHsaIdWhenCareUnitHsaIdIsProvided() {
      final var healthCareUnit = new HealthCareUnit();
      healthCareUnit.setHealthCareProviderHsaId(CARE_PROVIDER_HSA_ID);

      when(getHealthCareUnitService.get(
              GetHealthCareUnitRequestDTO.builder().hsaId(CARE_UNIT_HSA_ID).build()))
          .thenReturn(healthCareUnit);

      final var actualResult =
          hsaLegacyIntegrationOrganizationService.getVardgivareOfVardenhet(CARE_UNIT_HSA_ID);
      assertEquals(CARE_PROVIDER_HSA_ID, actualResult);
    }

    @Test
    void shouldReturnNull() {
      when(getHealthCareUnitService.get(
              GetHealthCareUnitRequestDTO.builder().hsaId(CARE_UNIT_HSA_ID).build()))
          .thenReturn(new HealthCareUnit());

      final var actualResult =
          hsaLegacyIntegrationOrganizationService.getVardgivareOfVardenhet(CARE_UNIT_HSA_ID);
      assertNull(actualResult);
    }
  }

  @Nested
  class GetVardenhet {

    @Test
    void shouldCallHsaLegacyGetCareUnitService() {
      when(getCareUnitService.get(CARE_UNIT_HSA_ID))
          .thenReturn(new Vardenhet(CARE_UNIT_HSA_ID, "CARE_UNIT_NAME"));

      final var careUnit = hsaLegacyIntegrationOrganizationService.getVardenhet(CARE_UNIT_HSA_ID);
      assertEquals(CARE_UNIT_HSA_ID, careUnit.getId());
    }

    @Test
    void shouldThrowWebServiceExceptionOnFetchUnitFailure() {
      when(getCareUnitService.get(CARE_UNIT_HSA_ID))
          .thenThrow(new WebServiceException("TestException"));

      assertThrows(
          WebServiceException.class,
          () -> hsaLegacyIntegrationOrganizationService.getVardenhet(CARE_UNIT_HSA_ID));
    }
  }

  @Nested
  class GetHsaIdForAktivaUnderenheter {

    private static final String CARE_UNIT_ID = "careUnitId";
    private static final String ACTIVE_CARE_UNIT_HSA_ID_1 = "careUnitId1";
    private static final String ACTIVE_CARE_UNIT_HSA_ID_2 = "careUnitId2";

    @Test
    void shouldReturnListOfHsaIdsForActiveSubUnits() {
      final var expectedResult = List.of(ACTIVE_CARE_UNIT_HSA_ID_1, ACTIVE_CARE_UNIT_HSA_ID_2);
      when(getHealthCareUnitMemberHsaIdService.get(
              GetHealthCareUnitMembersRequestDTO.builder().hsaId(CARE_UNIT_ID).build()))
          .thenReturn(expectedResult);
      final var result =
          hsaLegacyIntegrationOrganizationService.getHsaIdForAktivaUnderenheter(CARE_UNIT_ID);
      assertEquals(expectedResult, result);
    }
  }

  @Nested
  class GetParentUnit {

    private static final String CARE_UNIT_ID = "careUnitId";

    @Test
    void shouldThrowHsaServiceCallExceptionIfUnitIsNull() {
      when(getHealthCareUnitService.get(
              GetHealthCareUnitRequestDTO.builder().hsaId(CARE_UNIT_ID).build()))
          .thenReturn(null);

      assertThrows(
          HsaServiceCallException.class,
          () -> hsaLegacyIntegrationOrganizationService.getParentUnit(CARE_UNIT_ID));
    }

    @Test
    void shouldThrowHsaServiceCallException() {
      when(getHealthCareUnitService.get(
              GetHealthCareUnitRequestDTO.builder().hsaId(CARE_UNIT_ID).build()))
          .thenThrow(IllegalStateException.class);

      assertThrows(
          HsaServiceCallException.class,
          () -> hsaLegacyIntegrationOrganizationService.getParentUnit(CARE_UNIT_ID));
    }

    @Test
    void shouldReturnParentId() throws HsaServiceCallException {
      final var unit = new HealthCareUnit();
      unit.setHealthCareUnitHsaId(CARE_UNIT_ID);
      when(getHealthCareUnitService.get(
              GetHealthCareUnitRequestDTO.builder().hsaId(CARE_UNIT_ID).build()))
          .thenReturn(unit);

      final var result = hsaLegacyIntegrationOrganizationService.getParentUnit(CARE_UNIT_ID);

      assertEquals(CARE_UNIT_ID, result);
    }
  }

  @Nested
  class GetVardgivareInfo {

    private static final String CARE_UNIT_ID = "careUnitId";
    private static final String CARE_UNIT_NAME = "careUnitName";

    @Test
    void shouldReturnInfo() {
      final var unit = new Unit();
      unit.setUnitHsaId(CARE_UNIT_ID);
      unit.setUnitName(CARE_UNIT_NAME);
      when(getUnitService.get(GetUnitRequestDTO.builder().hsaId(CARE_UNIT_ID).build()))
          .thenReturn(unit);

      final var result = hsaLegacyIntegrationOrganizationService.getVardgivareInfo(CARE_UNIT_ID);

      assertEquals(new Vardgivare(CARE_UNIT_ID, CARE_UNIT_NAME), result);
    }

    @Test
    void shouldThrowErrorIfUnitIsNull() {
      when(getUnitService.get(GetUnitRequestDTO.builder().hsaId(CARE_UNIT_ID).build()))
          .thenReturn(null);

      assertThrows(
          WebServiceException.class,
          () -> hsaLegacyIntegrationOrganizationService.getVardgivareInfo(CARE_UNIT_ID));
    }
  }

  @Nested
  class TestGetAuthorizedEnheterForHosPerson {

    @Test
    void shouldGetCredentialInformationForPerson() {
      final var captor = ArgumentCaptor.forClass(GetCredentialInformationRequestDTO.class);
      hsaLegacyIntegrationOrganizationService.getAuthorizedEnheterForHosPerson("HSA_ID");

      verify(getCredentialInformationForPersonService).get(captor.capture());
      assertEquals("HSA_ID", captor.getValue().getPersonHsaId());
    }

    @Test
    void shouldSendCredentialInformationToGetAuthorizedInfoService() {
      final var expected = List.of(new CredentialInformation());
      when(getCredentialInformationForPersonService.get(any())).thenReturn(expected);

      final var captor = ArgumentCaptor.forClass(List.class);
      hsaLegacyIntegrationOrganizationService.getAuthorizedEnheterForHosPerson("HSA_ID");

      verify(getUserAuthorizationInfoService).get(captor.capture());
      assertEquals(expected, captor.getValue());
    }

    @Test
    void shouldReturnUserAuthenticationInfo() {
      final var expected = new UserAuthorizationInfo(null, Collections.emptyList(), null);
      when(getUserAuthorizationInfoService.get(any())).thenReturn(expected);

      final var response =
          hsaLegacyIntegrationOrganizationService.getAuthorizedEnheterForHosPerson("HSA_ID");

      assertEquals(expected, response);
    }
  }
}
