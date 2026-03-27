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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import jakarta.xml.ws.WebServiceException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.CredentialInformation;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HospCredentialsForPerson;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Result;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.authorization.GetCredentialInformationRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.authorization.GetHospCertificationPersonRequestDTO;

@ExtendWith(MockitoExtension.class)
class HsaIntegrationAuthorizationManagementServiceTest {

  private static final String PERSON_HSA_ID = "personHsaId";
  private static final String PERSON_ID = "personId";
  private static final String CERTIFICATION_ID = "certificationId";
  public static final String OPERATION = "operation";
  public static final String REASON = "reason";
  @Mock private GetCredentialInformationForPersonService credentialInformationForPersonService;

  @Mock GetHospLastUpdateService hospLastUpdateService;

  @Mock private GetHospCredentialsForPersonService getHospCredentialsForPersonService;

  @Mock private GetHospCertificationPersonService hospCertificationPersonService;

  @InjectMocks
  private HsaIntegrationAuthorizationManagementService hsaIntegrationAuthorizationManagementService;

  @Nested
  class GetCredentialInformationForPerson {

    @Test
    void shouldReturnListOfCredentialInformation() {
      final var expectedResponse = List.of(new CredentialInformation());

      final var request =
          GetCredentialInformationRequestDTO.builder().personHsaId(PERSON_HSA_ID).build();
      when(credentialInformationForPersonService.get(request)).thenReturn(expectedResponse);

      final var result =
          hsaIntegrationAuthorizationManagementService.getCredentialInformationForPerson(
              null, PERSON_HSA_ID, null);

      assertEquals(expectedResponse, result);
    }
  }

  @Nested
  class HandleHospCertificationPersonResponse {

    @Test
    void shouldReturnResultForHospCertificationPerson() {
      final var expectedResponse = new Result();

      final var request =
          GetHospCertificationPersonRequestDTO.builder()
              .personId(PERSON_ID)
              .certificationId(CERTIFICATION_ID)
              .reason(REASON)
              .operation(OPERATION)
              .build();

      when(hospCertificationPersonService.get(request)).thenReturn(expectedResponse);

      final var result =
          hsaIntegrationAuthorizationManagementService.handleHospCertificationPersonResponseType(
              CERTIFICATION_ID, OPERATION, PERSON_ID, REASON);

      assertEquals(expectedResponse, result);
    }
  }

  @Nested
  class GetHospLastUpdate {

    @Test
    void shouldReturnHospLastUpdate() {
      final var expectedResponse = LocalDateTime.now();

      when(hospLastUpdateService.get()).thenReturn(expectedResponse);

      final var result = hsaIntegrationAuthorizationManagementService.getHospLastUpdate();

      assertEquals(expectedResponse, result);
    }

    @Test
    void shouldThrowWebServiceExceptionIfIllegalStateExceptionIsCaught() {
      when(hospLastUpdateService.get()).thenThrow(IllegalStateException.class);

      assertThrows(
          WebServiceException.class,
          () -> hsaIntegrationAuthorizationManagementService.getHospLastUpdate());
    }
  }

  @Nested
  class GetHospCredentialsForPersonResponseType {

    @Test
    void shouldReturnCredentialsForPerson() {
      final var expectedResult = new HospCredentialsForPerson();

      when(getHospCredentialsForPersonService.get(PERSON_ID)).thenReturn(expectedResult);

      final var result =
          hsaIntegrationAuthorizationManagementService.getHospCredentialsForPersonResponseType(
              PERSON_ID);

      assertEquals(expectedResult, result);
    }

    @Test
    void shouldThrowWebServiceExceptionIfHospCredentialsForPersonIsNull() {
      when(getHospCredentialsForPersonService.get(PERSON_ID)).thenReturn(null);

      assertThrows(
          WebServiceException.class,
          () ->
              hsaIntegrationAuthorizationManagementService.getHospCredentialsForPersonResponseType(
                  PERSON_ID));
    }
  }
}
