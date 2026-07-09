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
package se.inera.intyg.webcert.integration.servicenow.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Base64;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SubscriptionRestClientTest {

  private MockRestServiceServer mockServer;
  private SubscriptionRestClient subscriptionRestClient;

  private static final String ORG_NO_1 = "ORG_NO_1";
  private static final String SUBSCRIPTION_URL = "https://servicenow.test";
  private static final List<String> SUBSCRIPTION_SERVICE_NAMES =
      List.of("Webcert-tj", "Webcert-int");
  private static final String SERVICENOW_USERNAME = "serviceNowUsername";
  private static final String SERVICENOW_PASSWORD = "serviceNowPassword";
  private static final String EMPTY_RESPONSE = "{\"result\":[]}";

  @BeforeEach
  void setup() {
    final var builder = RestClient.builder();
    mockServer = MockRestServiceServer.bindTo(builder).build();
    final var restClient = builder.build();

    subscriptionRestClient = new SubscriptionRestClient(restClient);
    ReflectionTestUtils.setField(subscriptionRestClient, "serviceNowUsername", SERVICENOW_USERNAME);
    ReflectionTestUtils.setField(subscriptionRestClient, "serviceNowPassword", SERVICENOW_PASSWORD);
    ReflectionTestUtils.setField(
        subscriptionRestClient, "serviceNowSubscriptionServiceUrl", SUBSCRIPTION_URL);
    ReflectionTestUtils.setField(
        subscriptionRestClient, "serviceNowSubscriptionServiceNames", SUBSCRIPTION_SERVICE_NAMES);
  }

  @Nested
  class TestRequest {

    @Test
    void shouldSetAuthorizationHeaderWithUsernameAndPassword() {
      mockServer
          .expect(requestTo(SUBSCRIPTION_URL))
          .andExpect(method(HttpMethod.POST))
          .andExpect(header("Authorization", getBasicAuthString()))
          .andRespond(withSuccess(EMPTY_RESPONSE, MediaType.APPLICATION_JSON));

      subscriptionRestClient.getSubscriptionServiceResponse(Set.of(ORG_NO_1));

      mockServer.verify();
    }

    @Test
    void shouldSetContentTypeHeaderToApplicationJson() {
      mockServer
          .expect(requestTo(SUBSCRIPTION_URL))
          .andExpect(method(HttpMethod.POST))
          .andExpect(header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
          .andRespond(withSuccess(EMPTY_RESPONSE, MediaType.APPLICATION_JSON));

      subscriptionRestClient.getSubscriptionServiceResponse(Set.of(ORG_NO_1));

      mockServer.verify();
    }

    @Test
    void shouldSetAcceptHeaderToApplicationJson() {
      mockServer
          .expect(requestTo(SUBSCRIPTION_URL))
          .andExpect(method(HttpMethod.POST))
          .andExpect(header("Accept", MediaType.APPLICATION_JSON_VALUE))
          .andRespond(withSuccess(EMPTY_RESPONSE, MediaType.APPLICATION_JSON));

      subscriptionRestClient.getSubscriptionServiceResponse(Set.of(ORG_NO_1));

      mockServer.verify();
    }

    @Test
    void shouldSetOrganizationRequestBodyWithProvidedServiceName() {
      mockServer
          .expect(requestTo(SUBSCRIPTION_URL))
          .andExpect(method(HttpMethod.POST))
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(content().json("{\"service\":[\"Webcert-tj\",\"Webcert-int\"]}"))
          .andRespond(withSuccess(EMPTY_RESPONSE, MediaType.APPLICATION_JSON));

      subscriptionRestClient.getSubscriptionServiceResponse(Set.of(ORG_NO_1));

      mockServer.verify();
    }

    @Test
    void shouldSetOrganizationRequestBodyWithProvidedOrganizationNumbers() {
      mockServer
          .expect(requestTo(SUBSCRIPTION_URL))
          .andExpect(method(HttpMethod.POST))
          .andExpect(content().json("{\"customers\":[\"" + ORG_NO_1 + "\"]}"))
          .andRespond(withSuccess(EMPTY_RESPONSE, MediaType.APPLICATION_JSON));

      subscriptionRestClient.getSubscriptionServiceResponse(Set.of(ORG_NO_1));

      mockServer.verify();
    }
  }

  @Test
  void shouldReturnOrganisationResponse() {
    final var responseJson = "{\"result\":[{\"orgNo\":\"" + ORG_NO_1 + "\",\"serviceCode\":[]}]}";
    mockServer
        .expect(requestTo(SUBSCRIPTION_URL))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    final var response = subscriptionRestClient.getSubscriptionServiceResponse(Set.of(ORG_NO_1));

    assertEquals(1, response.getResult().size());
    assertEquals(ORG_NO_1, response.getResult().getFirst().getOrganizationNumber());
  }

  @Test
  void shouldThrowIfResponseBodyIsNull() {
    mockServer.expect(requestTo(SUBSCRIPTION_URL)).andRespond(withNoContent());
    final var no1 = Set.of(ORG_NO_1);
    assertThrows(
        IllegalStateException.class,
        () -> subscriptionRestClient.getSubscriptionServiceResponse(no1));
  }

  private String getBasicAuthString() {
    final var authString = SERVICENOW_USERNAME + ":" + SERVICENOW_PASSWORD;
    return "Basic " + Base64.getEncoder().encodeToString(authString.getBytes());
  }
}
