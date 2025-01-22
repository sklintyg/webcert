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

package se.inera.intyg.webcert.integration.servicenow.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationRequest;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationResponse;

@ExtendWith(MockitoExtension.class)
class SubscriptionRestClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SubscriptionRestClient subscriptionRestClient;

    private static final String ORG_NO_1 = "ORG_NO_1";
    private static final String SUBSCRIPTION_URL = "https://servicenow.test";
    private static final String SUBSCRIPTION_SERVICE_NAME = "Webcert-tj";
    private static final String SERVICENOW_USERNAME = "serviceNowUsername";
    private static final String SERVICENOW_PASSWORD = "serviceNowPassword";

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(subscriptionRestClient, SERVICENOW_USERNAME, SERVICENOW_USERNAME);
        ReflectionTestUtils.setField(subscriptionRestClient, SERVICENOW_PASSWORD, SERVICENOW_PASSWORD);
        ReflectionTestUtils.setField(subscriptionRestClient, "serviceNowSubscriptionServiceUrl", SUBSCRIPTION_URL);
        ReflectionTestUtils.setField(subscriptionRestClient, "serviceNowSubscriptionServiceName", SUBSCRIPTION_SERVICE_NAME);
    }

    @Nested
    class TestRequest {

        @BeforeEach
        void setUp() {
            final var httpEntity = new ResponseEntity<>(OrganizationResponse.builder().build(), HttpStatus.ACCEPTED);
            doReturn(httpEntity).when(restTemplate)
                .exchange(eq(SUBSCRIPTION_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(OrganizationResponse.class));
        }

        @Test
        void shouldSetAuthorizationHeaderWithUsernameAndPassword() {
            final var captureHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);
            subscriptionRestClient.getSubscriptionServiceResponse(Set.of(ORG_NO_1));

            verify(restTemplate).exchange(any(String.class), any(HttpMethod.class), captureHttpEntity.capture(),
                eq(OrganizationResponse.class));
            assertTrue(
                Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Authorization")).contains(getBasicAuthString()));
        }


        @Test
        void shouldSetContentTypeHeaderToApplicationJson() {
            final var captureHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);
            subscriptionRestClient.getSubscriptionServiceResponse(Set.of(ORG_NO_1));

            verify(restTemplate).exchange(any(String.class), any(HttpMethod.class), captureHttpEntity.capture(),
                eq(OrganizationResponse.class));
            assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Content-Type")).contains("application/json"));
        }

        @Test
        void shouldSetAcceptHeaderToApplicationJson() {
            final var captureHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);
            subscriptionRestClient.getSubscriptionServiceResponse(Set.of(ORG_NO_1));

            verify(restTemplate).exchange(any(String.class), any(HttpMethod.class), captureHttpEntity.capture(),
                eq(OrganizationResponse.class));
            assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Accept")).contains("application/json"));
        }

        @Test
        void shouldSetOrganizationRequestBodyWithProvidedServiceName() {
            final var captureHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);
            subscriptionRestClient.getSubscriptionServiceResponse(Set.of(ORG_NO_1));

            verify(restTemplate).exchange(any(String.class), any(HttpMethod.class), captureHttpEntity.capture(),
                eq(OrganizationResponse.class));

            final var actualServiceName = ((OrganizationRequest) Objects.requireNonNull(
                captureHttpEntity.getValue().getBody())).getService();
            assertEquals(SUBSCRIPTION_SERVICE_NAME, actualServiceName);
        }

        @Test
        void shouldSetOrganizationRequestBodyWithProvidedOrganizationNumbers() {
            final var captureHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);
            subscriptionRestClient.getSubscriptionServiceResponse(Set.of(ORG_NO_1));

            verify(restTemplate).exchange(any(String.class), any(HttpMethod.class), captureHttpEntity.capture(),
                eq(OrganizationResponse.class));

            final var actualCustomers = ((OrganizationRequest) Objects.requireNonNull(
                captureHttpEntity.getValue().getBody())).getCustomers();
            assertEquals(List.of(ORG_NO_1), actualCustomers);
        }
    }


    @Test
    void shouldReturnOrganisationResponse() {
        final var expectedResponse = OrganizationResponse.builder()
            .build();
        final var httpEntity = new ResponseEntity<>(expectedResponse, HttpStatus.ACCEPTED);

        doReturn(httpEntity).when(restTemplate)
            .exchange(eq(SUBSCRIPTION_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(OrganizationResponse.class));

        final var actualResponse = subscriptionRestClient.getSubscriptionServiceResponse(Set.of(ORG_NO_1));
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void shouldThrowIfResponseBodyIsNull() {
        doReturn(new ResponseEntity<>(null, HttpStatus.ACCEPTED)).when(restTemplate)
            .exchange(eq(SUBSCRIPTION_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(OrganizationResponse.class));
        final var orgData = Set.of(ORG_NO_1);
        assertThrows(IllegalStateException.class, () -> subscriptionRestClient.getSubscriptionServiceResponse(orgData));
    }

    private String getBasicAuthString() {
        final var authString = SERVICENOW_USERNAME + ":" + SERVICENOW_PASSWORD;
        return "Basic " + Base64.getEncoder().encodeToString(authString.getBytes());
    }
}
