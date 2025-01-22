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
package se.inera.intyg.webcert.integration.kundportalen.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum.ELEG;
import static se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum.SITHS;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.webcert.integration.kundportalen.dto.OrganizationResponse;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionRestServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SubscriptionIntegrationServiceImpl subscriptionRestService;

    private static final ParameterizedTypeReference<List<OrganizationResponse>> LIST_ORGANIZATION_RESPONSE
        = new ParameterizedTypeReference<>() {
    };

    private static final List<String> ELEG_SERVICE_CODES = List.of("Webcert e-leg");
    private static final List<String> SITHS_SERVICE_CODES = List.of("Webcert integrerad-direktanslutning", "Webcert som Agent", "Webcert",
        "Webcert SITHS");

    @Before
    public void setup() {
        ReflectionTestUtils.setField(subscriptionRestService, "kundportalenAccessToken", "accessToken");
        ReflectionTestUtils.setField(subscriptionRestService, "kundportalenSubscriptionServiceUrl", "https://kp.test");
        ReflectionTestUtils.setField(subscriptionRestService, "kundportalenSubscriptionService", "Intygstjänster");
        ReflectionTestUtils.setField(subscriptionRestService, SubscriptionIntegrationServiceImpl.class, "elegServiceCodes",
            ELEG_SERVICE_CODES, List.class);
        ReflectionTestUtils.setField(subscriptionRestService, SubscriptionIntegrationServiceImpl.class, "sithsServiceCodes",
            SITHS_SERVICE_CODES, List.class);
    }

    // TESTS FOR SITHS USER

    @Test
    public void shouldReturnNoHsaIdsWhenSithsUserWithSingleOrgHasAnyNonElegSubscription() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturn(HttpStatus.OK, 1, 0, 1);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        assertTrue(response.isEmpty());
    }

    @Test
    public void shouldReturnNoHsaIdsWhenSithsUserWithSingleOrgHasMultipleSubscription() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturn(HttpStatus.OK, 1, 0, 3);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        assertTrue(response.isEmpty());
    }

    @Test
    public void shouldReturnNoHsaIdsWhenSithsUserWithMultipleOrgsWhichAllHaveSubscriptions() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturn(HttpStatus.OK, 1, 0, 1);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        assertTrue(response.isEmpty());
    }

    @Test
    public void shouldReturnAllCareProvidersSharingTheSameOrgNumber() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(2);
        orgNoHsaIdMap.get("ORG_NO_1").add("HSA_ID_1-2");
        orgNoHsaIdMap.get("ORG_NO_1").add("HSA_ID_1-3");
        final var expectedHsaIds = orgNoHsaIdMap.get("ORG_NO_1");

        setMockToReturn(HttpStatus.OK, 2, 2, 0);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        assertEquals(4, response.size());
        assertTrue(response.containsAll(expectedHsaIds));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenResponseBodyIsNullForSithsUser() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(2);

        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(LIST_ORGANIZATION_RESPONSE)))
            .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);
    }

    @Test(expected = RestClientException.class)
    public void shouldThrowRestClientExceptionReturnNoHsaIdsWhenServiceCallFailureForSithsUser() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(2);

        setMockToReturnRestClientException();

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        assertTrue(response.isEmpty());
    }

    @Test
    public void shouldReturnCorrectHsaIdWhenSithsUserWithSingleOrgHasNoSubscription() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturn(HttpStatus.OK, 1, 1, 1);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        assertEquals(1, response.size());
        assertEquals("HSA_ID_1", response.get(0));
    }

    @Test
    public void shouldReturnCorrectHsaIdsWhenSithsUserWithMultipleOrgsHaveNoSubscriptions() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(3);

        setMockToReturn(HttpStatus.OK, 3, 3, 0);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        assertEquals(3, response.size());
        assertTrue(response.containsAll(List.of("HSA_ID_1", "HSA_ID_2", "HSA_ID_3")));
    }

    @Test
    public void shouldReturnCorrectHsaIdsWhenSithsUserHasMultipleOrgsAndSomeMissingSubscription() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(4);

        setMockToReturn(HttpStatus.OK, 4, 2, 1);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        assertEquals(2, response.size());
        assertTrue(response.containsAll(List.of("HSA_ID_3", "HSA_ID_4")));
    }

    @Test
    public void shouldReturnCorrectHsaIdWhenSithsUserHasOnlyElegServiceCode() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);
        setMockToReturnElegServiceCode();

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        assertEquals(1, response.size());
        assertTrue(response.contains("HSA_ID_1"));
    }

    @Test
    public void shouldAddHeadersToKundportalenRestRequestForSithsUser() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);
        final var captureHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);

        setMockToReturn(HttpStatus.OK, 1, 0, 1);

        subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, SITHS);

        verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), captureHttpEntity.capture(),
            eq(LIST_ORGANIZATION_RESPONSE));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Authorization")).contains("accessToken"));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Content-Type")).contains("application/json"));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Accept")).contains("application/json"));
    }

    // TESTS FOR ELEG USER

    @Test
    public void shouldReturnNoHsaIdsWhenElegUserHasElegSubscription() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturnElegServiceCode();

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, ELEG);

        assertTrue(response.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenResponseBodyIsNullForElegUser() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, ELEG);
    }

    @Test(expected = RestClientException.class)
    public void shouldThrowRestClientExceptionWhenServiceCallFailureForElegUser() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturnRestClientException();

        subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, ELEG);
    }

    @Test
    public void shouldReturnCorrectHsaIdsWhenElegUserHasOnlyNonElegSubscriptions() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturn(HttpStatus.OK, 1, 0, 2);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, ELEG);

        assertEquals(1, response.size());
        assertTrue(response.contains("HSA_ID_1"));
    }

    @Test
    public void shouldReturnCorrectHsaIdWhenElegUserHasNoSubscription() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturn(HttpStatus.OK, 1, 1, 0);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, ELEG);

        assertEquals(1, response.size());
        assertEquals("HSA_ID_1", response.get(0));
    }

    @Test
    public void shouldAddHeadersToKundportalenRestRequestForElegUser() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);
        final var captureHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);

        setMockToReturnElegServiceCode();

        subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap, ELEG);

        verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), captureHttpEntity.capture(),
            eq(LIST_ORGANIZATION_RESPONSE));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Authorization")).contains("accessToken"));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Content-Type")).contains("application/json"));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Accept")).contains("application/json"));
    }

    // TESTS FOR UNREGISTERED ELEG USER

    @Test
    public void shouldReturnFalseWhenUnregisteredElegUserHasElegServiceCode() {
        setMockToReturnElegServiceCode();

        final var response = subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        assertFalse(response);
    }

    @Test
    public void shouldReturnTrueWhenUnregisteredElegUserHasOnlyNonElegServiceCode() {
        setMockToReturn(HttpStatus.ACCEPTED, 1, 1, 1);

        final var response = subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        assertTrue(response);
    }

    @Test
    public void shouldReturnTrueWhenUnregisteredElegUserHasNoActiveServiceCodes() {
        setMockToReturn(HttpStatus.ACCEPTED, 1, 1, 0);

        final var response = subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        assertTrue(response);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenResponseBodyIsNullForUnregisteredElegUser() {
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(LIST_ORGANIZATION_RESPONSE)))
            .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        final var response = subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        assertTrue(response);
    }

    @Test(expected = RestClientException.class)
    public void shouldThrowRestClientExceptionWhenServiceCallFailureForUnregisteredElegUser() {
        setMockToReturnRestClientException();

        subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");
    }

    @Test
    public void shouldAddHeadersToKundportalenRestRequestForUnregisteredElegUser() {
        final var captureHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);

        setMockToReturn(HttpStatus.OK, 1, 0, 1);

        subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), captureHttpEntity.capture(),
            eq(LIST_ORGANIZATION_RESPONSE));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Authorization")).contains("accessToken"));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Content-Type")).contains("application/json"));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Accept")).contains("application/json"));
    }


    private void setMockToReturn(HttpStatus httpStatus, int totalOrgsCount, int orgsMissingCount, int serviceCodeCount) {
        final var orgList = new ArrayList<OrganizationResponse>();
        orgsMissingCount = Math.min(orgsMissingCount, totalOrgsCount);

        for (var i = 1; i <= totalOrgsCount; i++) {
            if (i <= (totalOrgsCount - orgsMissingCount)) {
                orgList.add(createOrganization(i, serviceCodeCount));
            } else {
                orgList.add(createOrganization(i, 0));
            }
        }

        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(LIST_ORGANIZATION_RESPONSE)))
            .thenReturn(new ResponseEntity<>(orgList, httpStatus));
    }

    private void setMockToReturnElegServiceCode() {
        final var orgResponse = createOrganization(1, 0);
        orgResponse.setServiceCodes(ELEG_SERVICE_CODES);

        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(LIST_ORGANIZATION_RESPONSE)))
            .thenReturn(new ResponseEntity<>(List.of(orgResponse), HttpStatus.OK));
    }

    private void setMockToReturnRestClientException() {
        final var e = new RestClientException("MESSAGE_TEXT");
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(LIST_ORGANIZATION_RESPONSE)))
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

    private OrganizationResponse createOrganization(int orgNo, int serviceCodeCount) {
        final var orgResponse = new OrganizationResponse();
        final var serviceCodeList = createSithsServiceCodeList(serviceCodeCount);
        orgResponse.setOrganizationNumber("ORG_NO_" + orgNo);
        orgResponse.setServiceCodes(serviceCodeList);
        return orgResponse;
    }

    private List<String> createSithsServiceCodeList(int serviceCodeCount) {
        final var serviceCodeList = new ArrayList<String>();
        for (var i = 1; i <= serviceCodeCount; i++) {
            serviceCodeList.add(SITHS_SERVICE_CODES.get(i));
        }
        return serviceCodeList;
    }
}
