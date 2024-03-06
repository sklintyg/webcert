/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.integration.servicenow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum.ELEG;
import static se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum.SITHS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.webcert.integration.servicenow.dto.Organization;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationResponse;

@ExtendWith(MockitoExtension.class)
class ServiceNowSubscriptionRestServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ServiceNowSubscriptionRestServiceImpl subscriptionRestService;

    private static final String SERVICENOW_USERNAME = "serviceNowUsername";
    private static final String SERVICENOW_PASSWORD = "serviceNowPassword";
    private static final String SUBSCRIPTION_SERVICE_NAME = "Webcert-tj";

    private static final List<String> ELEG_SERVICE_CODES = List.of("Webcert fristående med e-legitimation");
    private static final List<String> SITHS_SERVICE_CODES = List.of("Webcert fristående med SITHS-kort", "Webcert Integrerad - via agent",
        "Webcert Integrerad - via region", "Webcert integrerad - direktanslutning");

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(subscriptionRestService, SERVICENOW_USERNAME, SERVICENOW_USERNAME);
        ReflectionTestUtils.setField(subscriptionRestService, SERVICENOW_PASSWORD, SERVICENOW_PASSWORD);
        ReflectionTestUtils.setField(subscriptionRestService, "serviceNowSubscriptionServiceUrl", "https://servicenow.test");
        ReflectionTestUtils.setField(subscriptionRestService, "serviceNowSubscriptionService", SUBSCRIPTION_SERVICE_NAME);
        ReflectionTestUtils.setField(subscriptionRestService, ServiceNowSubscriptionRestServiceImpl.class, "elegServiceCodes",
            ELEG_SERVICE_CODES, List.class);
        ReflectionTestUtils.setField(subscriptionRestService, ServiceNowSubscriptionRestServiceImpl.class, "sithsServiceCodes",
            SITHS_SERVICE_CODES, List.class);
    }

    // TESTS FOR SITHS USER

    @ParameterizedTest
    @CsvSource({ "1, 1", "1, 3", "3, 1" })
    void shouldReturnNoHsaIdsWhenSithsUserHasOneOrMoreSubscriptions(int hsaIdCount, int serviceCodeCount) {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(hsaIdCount);
        setMockToReturn(HttpStatus.OK, 1, 0, serviceCodeCount);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        assertTrue(response.isEmpty());
    }

    @Test
    void shouldReturnAllCareProvidersSharingTheSameOrgNumber() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(2);
        orgNoHsaIdMap.get("ORG_NO_1").add("HSA_ID_1-2");
        orgNoHsaIdMap.get("ORG_NO_1").add("HSA_ID_1-3");
        final var expectedHsaIds = orgNoHsaIdMap.get("ORG_NO_1");
        setMockToReturn(HttpStatus.OK, 2, 2, 0);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        assertEquals(4, response.size());
        assertTrue(response.containsAll(expectedHsaIds));
    }

    @Test
    void shouldThrowNullPointerExceptionWhenResponseBodyIsNullForSithsUser() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(2);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(OrganizationResponse.class)))
            .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThrows(NullPointerException.class, () ->
            subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS)
        );
    }

    @Test
    void shouldThrowRestClientExceptionReturnNoHsaIdsWhenServiceCallFailureForSithsUser() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(2);

        setMockToReturnRestClientException();

        assertThrows(RestClientException.class, () ->
            subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS)
        );
    }

    @Test
    void shouldReturnCorrectHsaIdWhenSithsUserWithSingleOrgHasNoSubscription() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturn(HttpStatus.OK, 1, 1, 1);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        assertEquals(1, response.size());
        assertEquals("HSA_ID_1", response.get(0));
    }

    @Test
    void shouldReturnCorrectHsaIdsWhenSithsUserWithMultipleOrgsHaveNoSubscriptions() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(3);

        setMockToReturn(HttpStatus.OK, 3, 3, 0);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        assertEquals(3, response.size());
        assertTrue(response.containsAll(List.of("HSA_ID_1", "HSA_ID_2", "HSA_ID_3")));
    }

    @Test
    void shouldReturnCorrectHsaIdsWhenSithsUserHasMultipleOrgsAndSomeMissingSubscription() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(4);

        setMockToReturn(HttpStatus.OK, 4, 2, 1);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        assertEquals(2, response.size());
        assertTrue(response.containsAll(List.of("HSA_ID_3", "HSA_ID_4")));
    }

    @Test
    void shouldReturnCorrectHsaIdWhenSithsUserHasOnlyElegServiceCode() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);
        setMockToReturnElegServiceCode();

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        assertEquals(1, response.size());
        assertTrue(response.contains("HSA_ID_1"));
    }

    @Test
    void shouldAddHeadersToServicenownRestRequestForSithsUser() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);
        final var captureHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);

        setMockToReturn(HttpStatus.OK, 1, 0, 1);

        subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        verify(restTemplate).exchange(any(String.class), any(HttpMethod.class), captureHttpEntity.capture(),
            eq(OrganizationResponse.class));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Authorization")).contains(getBasicAuthString()));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Content-Type")).contains("application/json"));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Accept")).contains("application/json"));
    }

    // TESTS FOR ELEG USER

    @Test
    void shouldReturnNoHsaIdsWhenElegUserHasElegSubscription() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturnElegServiceCode();

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, ELEG);

        assertTrue(response.isEmpty());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenResponseBodyIsNullForElegUser() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        assertThrows(NullPointerException.class, () ->
            subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, ELEG)
        );
    }

    @Test
    void shouldThrowRestClientExceptionWhenServiceCallFailureForElegUser() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);
        setMockToReturnRestClientException();

        assertThrows(RestClientException.class, () ->
            subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, ELEG)
        );
    }

    @Test
    void shouldReturnCorrectHsaIdsWhenElegUserHasOnlyNonElegSubscriptions() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturn(HttpStatus.OK, 1, 0, 2);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, ELEG);

        assertEquals(1, response.size());
        assertTrue(response.contains("HSA_ID_1"));
    }

    @Test
    void shouldReturnCorrectHsaIdWhenElegUserHasNoSubscription() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturn(HttpStatus.OK, 1, 1, 0);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, ELEG);

        assertEquals(1, response.size());
        assertEquals("HSA_ID_1", response.get(0));
    }

    @Test
    void shouldAddHeadersToServiceNowRestRequestForElegUser() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);
        final var captureHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);

        setMockToReturnElegServiceCode();

        subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, ELEG);

        verify(restTemplate).exchange(any(String.class), any(HttpMethod.class), captureHttpEntity.capture(),
            eq(OrganizationResponse.class));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Authorization")).contains(getBasicAuthString()));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Content-Type")).contains("application/json"));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Accept")).contains("application/json"));
    }

    // TESTS FOR UNREGISTERED ELEG USER

    @Test
    void shouldReturnFalseWhenUnregisteredElegUserHasElegServiceCode() {
        setMockToReturnElegServiceCode();

        final var response = subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        assertFalse(response);
    }

    @Test
    void shouldReturnTrueWhenUnregisteredElegUserHasOnlyNonElegServiceCode() {
        setMockToReturn(HttpStatus.ACCEPTED, 1, 1, 1);

        final var response = subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        assertTrue(response);
    }

    @Test
    void shouldReturnTrueWhenUnregisteredElegUserHasNoActiveServiceCodes() {
        setMockToReturn(HttpStatus.ACCEPTED, 1, 1, 0);

        final var response = subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        assertTrue(response);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenResponseBodyIsNullForUnregisteredElegUser() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(OrganizationResponse.class)))
            .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThrows(NullPointerException.class, () ->
            subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1")
        );
    }

    @Test
    void shouldThrowRestClientExceptionWhenServiceCallFailureForUnregisteredElegUser() {
        setMockToReturnRestClientException();

        assertThrows(RestClientException.class, () ->
            subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1")
        );
    }

    @Test
    void shouldAddHeadersToServicenowRestRequestForUnregisteredElegUser() {
        final var captureHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);

        setMockToReturn(HttpStatus.OK, 1, 0, 1);

        subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        verify(restTemplate).exchange(any(String.class), any(HttpMethod.class), captureHttpEntity.capture(),
            eq(OrganizationResponse.class));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Authorization")).contains(getBasicAuthString()));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Content-Type")).contains("application/json"));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Accept")).contains("application/json"));
    }


    private void setMockToReturn(HttpStatus httpStatus, int totalOrgsCount, int orgsMissingCount, int serviceCodeCount) {
        final var orgList = new ArrayList<Organization>();
        orgsMissingCount = Math.min(orgsMissingCount, totalOrgsCount);

        for (var i = 1; i <= totalOrgsCount; i++) {
            if (i <= (totalOrgsCount - orgsMissingCount)) {
                orgList.add(createOrganization(i, serviceCodeCount));
            } else {
                orgList.add(createOrganization(i, 0));
            }
        }
        final var organizationResponse = OrganizationResponse.builder()
                .result(orgList).build();
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(OrganizationResponse.class)))
            .thenReturn(new ResponseEntity<>(organizationResponse, httpStatus));
    }

    private void setMockToReturnElegServiceCode() {
        final var organizationResponse = OrganizationResponse.builder()
            .result(
                List.of(Organization.builder()
                        .organizationNumber("ORG_NO_1")
                        .serviceCodes(ELEG_SERVICE_CODES)
                        .build())
            ).build();

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(OrganizationResponse.class)))
            .thenReturn(new ResponseEntity<>(organizationResponse, HttpStatus.OK));
    }

    private void setMockToReturnRestClientException() {
        final var e = new RestClientException("MESSAGE_TEXT");
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(OrganizationResponse.class)))
            .thenThrow(e);
    }

    private Map<String, List<String>> createOrgNoHsaIdMap(int count) {
        final var orgNoHsaIdMap = new HashMap<String, List<String>>();
        for (var i = 1; i <= count; i++) {
            final var hsaIdList = new ArrayList<String>();
            hsaIdList.add("HSA_ID_" + i);
            orgNoHsaIdMap.put("ORG_NO_" + i, hsaIdList);
        }
        return orgNoHsaIdMap;
    }

    private Organization createOrganization(int orgNo, int serviceCodeCount) {
        final var serviceCodeList = createSithsServiceCodeList(serviceCodeCount);
        return Organization.builder()
            .organizationNumber("ORG_NO_" + orgNo)
            .serviceCodes(serviceCodeList)
            .build();
    }

    private List<String> createSithsServiceCodeList(int serviceCodeCount) {
        final var serviceCodeList = new ArrayList<String>();
        for (var i = 1; i <= serviceCodeCount; i++) {
            serviceCodeList.add(SITHS_SERVICE_CODES.get(i));
        }
        return serviceCodeList;
    }

    private String getBasicAuthString() {
        final var authString = SERVICENOW_USERNAME + ":" + SERVICENOW_PASSWORD;
        return "Basic " + Base64.getEncoder().encodeToString(authString.getBytes());
    }
}
