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

package se.inera.intyg.webcert.integration.privatepractitioner.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_CARE_UNIT_NAME;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_HSA_ID;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_NAME;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_PERSON_ID;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataConstants.DR_KRANSTEGE_POSITION;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataDTO.validRegisterPractitionerRequest;
import static se.inera.intyg.webcert.integration.privatepractitioner.service.TestDataModel.DR_KRANSTEGE;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PrivatePractitioner;
import se.inera.intyg.webcert.integration.privatepractitioner.model.RegisterPrivatePractitionerRequest;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.logging.MdcLogConstants;

@ExtendWith(MockitoExtension.class)
class RegisterPrivatePractitionerClientTest {

  private static final String SESSION_ID = "session-123";
  private static final String TRACE_ID = "trace-456";

  @Mock
  private RestClient ppsRestClient;

  @InjectMocks
  private RegisterPrivatePractitionerClient registerPrivatePractitionerClient;

  @Captor
  private ArgumentCaptor<RegisterPrivatePractitionerRequest> requestCaptor;

  private RestClient.RequestBodyUriSpec requestBodyUriSpec;
  private RestClient.ResponseSpec responseSpec;

  @BeforeEach
  void setUp() {
    MDC.put(MdcLogConstants.SESSION_ID_KEY, SESSION_ID);
    MDC.put(MdcLogConstants.TRACE_ID_KEY, TRACE_ID);

    requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
    responseSpec = mock(RestClient.ResponseSpec.class);

    when(ppsRestClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.body(any(RegisterPrivatePractitionerRequest.class))).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
  }

  @Test
  void shouldRegisterPrivatePractitioner() {
    final var registrationRequest = validRegisterPractitionerRequest();

    final var expectedResponse = DR_KRANSTEGE;

    when(responseSpec.body(PrivatePractitioner.class)).thenReturn(expectedResponse);

    final var result = registerPrivatePractitionerClient.registerPrivatePractitioner(registrationRequest);

    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals(DR_KRANSTEGE_HSA_ID, result.getHsaId()),
        () -> assertEquals(DR_KRANSTEGE_PERSON_ID, result.getPersonId()),
        () -> assertEquals(DR_KRANSTEGE_NAME, result.getName()),
        () -> assertEquals(DR_KRANSTEGE_POSITION, result.getPosition()),
        () -> assertEquals(DR_KRANSTEGE_CARE_UNIT_NAME, result.getCareUnitName()),
        () -> assertEquals(DR_KRANSTEGE_CARE_UNIT_NAME, result.getCareProviderName()),
        () -> assertNotNull(result.getRegistrationDate())
    );
  }

  @Test
  void shouldSetCorrectHeaders() {
    final var registrationRequest = RegisterPrivatePractitionerRequest.builder()
        .personId(DR_KRANSTEGE_PERSON_ID)
        .name(DR_KRANSTEGE_NAME)
        .build();

    final var expectedResponse = PrivatePractitioner.builder()
        .hsaId(DR_KRANSTEGE_HSA_ID)
        .personId(DR_KRANSTEGE_PERSON_ID)
        .name(DR_KRANSTEGE_NAME)
        .build();

    when(responseSpec.body(PrivatePractitioner.class)).thenReturn(expectedResponse);

    registerPrivatePractitionerClient.registerPrivatePractitioner(registrationRequest);

    verify(requestBodyUriSpec).header(MdcHelper.LOG_SESSION_ID_HEADER, SESSION_ID);
    verify(requestBodyUriSpec).header(MdcHelper.LOG_TRACE_ID_HEADER, TRACE_ID);
  }

  @Test
  void shouldSendCorrectRequestBody() {
    final var registrationRequest = validRegisterPractitionerRequest();

    final var expectedResponse = DR_KRANSTEGE;

    when(responseSpec.body(PrivatePractitioner.class)).thenReturn(expectedResponse);

    registerPrivatePractitionerClient.registerPrivatePractitioner(registrationRequest);

    verify(requestBodyUriSpec).body(requestCaptor.capture());
    final var capturedRequest = requestCaptor.getValue();

    assertAll(
        () -> assertEquals(DR_KRANSTEGE_PERSON_ID, capturedRequest.getPersonId()),
        () -> assertEquals(DR_KRANSTEGE_NAME, capturedRequest.getName()),
        () -> assertEquals(DR_KRANSTEGE_POSITION, capturedRequest.getPosition()),
        () -> assertEquals(DR_KRANSTEGE_CARE_UNIT_NAME, capturedRequest.getCareUnitName())
    );
  }
}