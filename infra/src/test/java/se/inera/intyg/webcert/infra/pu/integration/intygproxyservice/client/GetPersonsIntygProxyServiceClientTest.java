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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.configuration.PURestClientConfig.LOG_SESSION_ID_HEADER;
import static se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.configuration.PURestClientConfig.LOG_TRACE_ID_HEADER;
import static se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.configuration.PURestClientConfig.SESSION_ID_KEY;
import static se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.configuration.PURestClientConfig.TRACE_ID_KEY;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import se.inera.intyg.webcert.infra.pu.integration.api.model.Person;
import se.inera.intyg.webcert.infra.pu.integration.api.model.PersonSvar.Status;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.client.GetPersonsIntygProxyServiceClient;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.dto.PersonResponseDTO;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.dto.PersonsRequestDTO;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.dto.PersonsResponseDTO;

@ExtendWith(MockitoExtension.class)
class GetPersonsIntygProxyServiceClientTest {

  private static final String ENDPOINT = "endpoint";
  private static final String TRACE_ID = "traceId";
  private static final String SESSION_ID = "sessionId";
  private final RestClient.RequestBodyUriSpec requestBodyUriSpec =
      mock(RestClient.RequestBodyUriSpec.class);
  private final RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
  private final RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

  @Mock private RestClient restClient;
  @InjectMocks private GetPersonsIntygProxyServiceClient getPersonsIntygProxyServiceClient;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(getPersonsIntygProxyServiceClient, "personsEndpoint", ENDPOINT);
    MDC.put(TRACE_ID_KEY, TRACE_ID);
    MDC.put(SESSION_ID_KEY, SESSION_ID);
  }

  @Test
  void shallReturnPersonResponse() {
    final var request =
        PersonsRequestDTO.builder().personIds(List.of("personId")).queryCache(true).build();

    final var expectedResponse =
        PersonsResponseDTO.builder()
            .persons(
                List.of(
                    PersonResponseDTO.builder()
                        .status(Status.FOUND)
                        .person(mock(Person.class))
                        .build()))
            .build();

    doReturn(requestBodyUriSpec).when(restClient).post();
    doReturn(requestBodySpec).when(requestBodyUriSpec).uri(ENDPOINT);
    doReturn(requestBodySpec).when(requestBodySpec).body(request);
    doReturn(requestBodySpec).when(requestBodySpec).header(LOG_TRACE_ID_HEADER, TRACE_ID);
    doReturn(requestBodySpec).when(requestBodySpec).header(LOG_SESSION_ID_HEADER, SESSION_ID);
    doReturn(requestBodySpec).when(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
    doReturn(responseSpec).when(requestBodySpec).retrieve();
    doReturn(expectedResponse).when(responseSpec).body(PersonsResponseDTO.class);

    final var response = getPersonsIntygProxyServiceClient.get(request);

    assertEquals(expectedResponse, response);
  }

  @Test
  void shallSetHeadersCorrectly() {
    final var request =
        PersonsRequestDTO.builder().personIds(List.of("personId")).queryCache(true).build();

    doReturn(requestBodyUriSpec).when(restClient).post();
    doReturn(requestBodySpec).when(requestBodyUriSpec).uri(ENDPOINT);
    doReturn(requestBodySpec).when(requestBodySpec).body(request);
    doReturn(requestBodySpec).when(requestBodySpec).header(LOG_TRACE_ID_HEADER, TRACE_ID);
    doReturn(requestBodySpec).when(requestBodySpec).header(LOG_SESSION_ID_HEADER, SESSION_ID);
    doReturn(requestBodySpec).when(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
    doReturn(responseSpec).when(requestBodySpec).retrieve();
    doReturn(mock(PersonsResponseDTO.class)).when(responseSpec).body(PersonsResponseDTO.class);

    getPersonsIntygProxyServiceClient.get(request);

    verify(requestBodySpec).header(LOG_TRACE_ID_HEADER, TRACE_ID);
    verify(requestBodySpec).header(LOG_SESSION_ID_HEADER, SESSION_ID);
  }

  @Test
  void shallSetContentTypeAsApplicationJson() {
    final var request =
        PersonsRequestDTO.builder().personIds(List.of("personId")).queryCache(true).build();

    doReturn(requestBodyUriSpec).when(restClient).post();
    doReturn(requestBodySpec).when(requestBodyUriSpec).uri(ENDPOINT);
    doReturn(requestBodySpec).when(requestBodySpec).body(request);
    doReturn(requestBodySpec).when(requestBodySpec).header(LOG_TRACE_ID_HEADER, TRACE_ID);
    doReturn(requestBodySpec).when(requestBodySpec).header(LOG_SESSION_ID_HEADER, SESSION_ID);
    doReturn(requestBodySpec).when(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
    doReturn(responseSpec).when(requestBodySpec).retrieve();
    doReturn(mock(PersonsResponseDTO.class)).when(responseSpec).body(PersonsResponseDTO.class);

    getPersonsIntygProxyServiceClient.get(request);

    verify(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
  }
}
