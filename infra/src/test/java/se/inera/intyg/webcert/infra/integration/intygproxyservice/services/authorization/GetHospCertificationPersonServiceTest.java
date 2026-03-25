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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.services.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Result;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.client.authorization.HsaIntygProxyServiceHospCertificationPersonClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.authorization.GetHospCertificationPersonRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.authorization.GetHospCertificationPersonResponseDTO;

@ExtendWith(MockitoExtension.class)
class GetHospCertificationPersonServiceTest {

  private static final String PERSON_ID = "personId";
  private static final String CERTIFICATION_ID = "certificationId";
  public static final String OPERATION = "operation";
  public static final String REASON = "reason";
  private static final Result RESULT = new Result();
  private static final String RESULT_TEXT = "resultText";
  private static final String RESULT_CODE = "resultCode";

  @Mock private HsaIntygProxyServiceHospCertificationPersonClient hospCertificationPersonClient;

  @InjectMocks private GetHospCertificationPersonService getHospCertificationPersonService;

  @BeforeEach
  void setup() {
    RESULT.setResultText(RESULT_TEXT);
    RESULT.setResultCode(RESULT_CODE);
  }

  @Test
  void shouldReturnResultForHospCertificationPerson() {
    final var request =
        GetHospCertificationPersonRequestDTO.builder()
            .personId(PERSON_ID)
            .certificationId(CERTIFICATION_ID)
            .operation(OPERATION)
            .reason(REASON)
            .build();

    final var expectedResponse =
        GetHospCertificationPersonResponseDTO.builder().result(RESULT).build();

    when(hospCertificationPersonClient.get(request)).thenReturn(expectedResponse);

    final var result = getHospCertificationPersonService.get(request);

    assertEquals(expectedResponse.getResult(), result);
  }

  @Nested
  class ShouldValidateRequest {

    @Test
    void shouldThrowIfPersonIdIsNull() {
      final var request =
          GetHospCertificationPersonRequestDTO.builder()
              .personId(null)
              .certificationId(CERTIFICATION_ID)
              .operation(OPERATION)
              .reason(REASON)
              .build();

      assertThrows(
          IllegalArgumentException.class, () -> getHospCertificationPersonService.get(request));
    }

    @Test
    void shouldThrowIfPersonIdIsEmpty() {
      final var request =
          GetHospCertificationPersonRequestDTO.builder()
              .personId("")
              .certificationId(CERTIFICATION_ID)
              .operation(OPERATION)
              .reason(REASON)
              .build();

      assertThrows(
          IllegalArgumentException.class, () -> getHospCertificationPersonService.get(request));
    }

    @Test
    void shouldThrowIfOperationIsNull() {
      final var request =
          GetHospCertificationPersonRequestDTO.builder()
              .personId(PERSON_ID)
              .certificationId(CERTIFICATION_ID)
              .operation(null)
              .reason(REASON)
              .build();

      assertThrows(
          IllegalArgumentException.class, () -> getHospCertificationPersonService.get(request));
    }

    @Test
    void shouldThrowIfOperationIsEmpty() {
      final var request =
          GetHospCertificationPersonRequestDTO.builder()
              .personId(PERSON_ID)
              .certificationId(CERTIFICATION_ID)
              .operation("")
              .reason(REASON)
              .build();

      assertThrows(
          IllegalArgumentException.class, () -> getHospCertificationPersonService.get(request));
    }
  }
}
