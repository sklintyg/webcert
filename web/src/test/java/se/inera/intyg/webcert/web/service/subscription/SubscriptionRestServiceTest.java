/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.subscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.schemas.contract.util.HashUtility;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.subscription.dto.OrganizationResponse;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionRestServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private SubscriptionRestServiceImpl subscriptionRestService;

    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 6, 13, 22, 13, 28);

    private static final ParameterizedTypeReference<List<OrganizationResponse>> LIST_ORGANIZATION_RESPONSE
        = new ParameterizedTypeReference<>() { };

    @Before
    public void setup() {
        ReflectionTestUtils.setField(subscriptionRestService, "kundportalenAccessToken", "accessToken");
        ReflectionTestUtils.setField(subscriptionRestService, "kundportalenSubscriptionServiceUrl", "serviceUrl");
    }

    @Test
    public void shouldReturnNoHsaIdsWhenSingleOrgHasSingleSubscription() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturn(HttpStatus.OK, 1, 0, 1);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap);

        assertTrue(response.isEmpty());
    }

    @Test
    public void shouldReturnNoHsaIdsWhenSingleOrgHasMultipleSubscription() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturn(HttpStatus.OK, 1, 0, 3);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap);

        assertTrue(response.isEmpty());
    }

    @Test
    public void shouldReturnNoHsaIdsWhenMultipleOrgsAllHaveSubscriptions() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturn(HttpStatus.OK, 1, 0, 1);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap);

        assertTrue(response.isEmpty());
    }

    @Test
    public void shouldReturnCorrectHsaIdWhenSingleOrgHasNoSubscription() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);

        setMockToReturn(HttpStatus.OK, 1, 1, 1);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap);

        assertEquals(1, response.size());
        assertEquals("HSA_ID_1", response.get(0));
    }

    @Test
    public void shouldReturnCorrectHsaIdsWhenMultipleOrgHaveNoSubscriptions() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(3);

        setMockToReturn(HttpStatus.OK, 3, 3, 0);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap);

        assertEquals(3, response.size());
        assertTrue(response.containsAll(List.of("HSA_ID_1", "HSA_ID_2", "HSA_ID_3")));
    }

    @Test
    public void shouldReturnCorrectHsaIdsWhenMultipleOrgsAndSomeMissingSubscription() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(4);

        setMockToReturn(HttpStatus.OK, 4, 2, 1);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap);

        assertEquals(2, response.size());
        assertTrue(response.containsAll(List.of("HSA_ID_3", "HSA_ID_4")));
    }

    @Test
    public void shouldReturnNoHsaIdsWhenHttpServerErrorException() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(2);

        setMockToReturnHttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap);

        assertTrue(response.isEmpty());
    }

    @Test
    public void shouldReturnNoHsaIdsWhenHttpClientErrorException() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(2);

        setMockToReturnHttpServerErrorException(HttpStatus.FORBIDDEN);

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap);

        assertTrue(response.isEmpty());
    }

    @Test
    public void shouldReturnNoHsaIdsWhenResponseHasNoBody() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(2);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(LIST_ORGANIZATION_RESPONSE)))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap);

        assertTrue(response.isEmpty());
    }

    @Test
    public void shouldReturnNoHsaIdsWhenResponseBodyIsNull() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(2);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(LIST_ORGANIZATION_RESPONSE)))
            .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        final var response = subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap);

        assertTrue(response.isEmpty());
    }

    @Test
    public void shouldMonitorlogWhenHttpServerErrorException() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(2);
        final var queryIds = orgNoHsaIdMap.values();
        setMockToReturnHttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap);

        verify(monitoringLogService).logSubscriptionServiceCallFailure(queryIds, 500, "INTERNAL_SERVER_ERROR", "MESSAGE_TEXT", LOCAL_DATE_TIME);
    }

    @Test
    public void shouldMonitorlogWhenHttpClientErrorException() {
        final var queryIds = Collections.singleton(HashUtility.hash("ORG_NO_1"));

        setMockToReturnHttpClientErrorException(HttpStatus.FORBIDDEN);

        subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        verify(monitoringLogService).logSubscriptionServiceCallFailure(queryIds, 403, "FORBIDDEN", "MESSAGE_TEXT", LOCAL_DATE_TIME);
    }

    @Test
    public void shouldAddHeadersToKundportalenRestRequest() {
        final var orgNoHsaIdMap = createOrgNoHsaIdMap(1);
        final var captureHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);

        setMockToReturn(HttpStatus.OK, 1, 0, 1);

        subscriptionRestService.getMissingSubscriptions(orgNoHsaIdMap);

        verify(restTemplate).exchange(any(String.class), any(HttpMethod.class), captureHttpEntity.capture(),
            eq(LIST_ORGANIZATION_RESPONSE));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Authorization")).contains("accessToken"));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Content-Type")).contains("application/json"));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Accept")).contains("application/json"));
    }

    @Test
    public void shouldReturnFalseWhenUnregisteredElegUserHasSubscription() {
        setMockToReturn(HttpStatus.OK, 1, 0, 1);

        final var response = subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        assertFalse(response);
    }

    @Test
    public void shouldReturnTrueWhenUnregisteredElegUserMissingSubscription() {
        setMockToReturn(HttpStatus.ACCEPTED, 1, 1, 0);

        final var response = subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        assertTrue(response);
    }

    @Test
    public void shouldReturnTrueWhenHttpServerErrorExceptionForUnregElegUser() {
        setMockToReturnHttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        final var response = subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        assertTrue(response);
    }

    @Test
    public void shouldReturnTrueWhenHttpClientErrorExceptionForUnregElegUser() {
        setMockToReturnHttpServerErrorException(HttpStatus.FORBIDDEN);

        final var response = subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        assertTrue(response);
    }

    @Test
    public void shouldReturnTrueWhenResponseHasNoBodyForUnregElegUser() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(LIST_ORGANIZATION_RESPONSE)))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        final var response = subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        assertTrue(response);
    }

    @Test
    public void shouldReturnTrueWhenResponseBodyIsNullForUnregElegUser() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(LIST_ORGANIZATION_RESPONSE)))
            .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        final var response = subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        assertTrue(response);
    }

    @Test
    public void shouldAddHeadersToKundportalenRestRequestForUnregElegUser() {
        final var captureHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);

        setMockToReturn(HttpStatus.OK, 1, 0, 1);

        subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        verify(restTemplate).exchange(any(String.class), any(HttpMethod.class), captureHttpEntity.capture(),
            eq(LIST_ORGANIZATION_RESPONSE));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Authorization")).contains("accessToken"));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Content-Type")).contains("application/json"));
        assertTrue(Objects.requireNonNull(captureHttpEntity.getValue().getHeaders().get("Accept")).contains("application/json"));
    }

    @Test
    public void shouldMonitorlogWhenHttpServerErrorExceptionForUnregElegUser() {
        final var queryIds = Collections.singleton(HashUtility.hash("ORG_NO_1"));

        setMockToReturnHttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE);

        subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        verify(monitoringLogService).logSubscriptionServiceCallFailure(queryIds, 503, "SERVICE_UNAVAILABLE", "MESSAGE_TEXT", LOCAL_DATE_TIME);
    }

    @Test
    public void shouldMonitorlogWhenHttpClientErrorExceptionForUnregElegUser() {
        final var queryIds = Collections.singleton(HashUtility.hash("ORG_NO_1"));
        setMockToReturnHttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE);

        subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        verify(monitoringLogService).logSubscriptionServiceCallFailure(queryIds, 503, "SERVICE_UNAVAILABLE", "MESSAGE_TEXT", LOCAL_DATE_TIME);
    }

    @Test
    public void shouldSetEmptyStatusTextInMonitorLogIfCustomErrorCode() {
        final var queryIds = Collections.singleton(HashUtility.hash("ORG_NO_1"));

        setMockToReturnRestClientResponseException();

        subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        verify(monitoringLogService).logSubscriptionServiceCallFailure(queryIds, 777, "", "MESSAGE_TEXT", null);
    }

    @Test
    public void shouldMonitorlogWhenRestClientException() {
        final var queryIds = Collections.singleton(HashUtility.hash("ORG_NO_1"));
        setMockToReturnRestClientException();

        subscriptionRestService.isMissingSubscriptionUnregisteredElegUser("ORG_NO_1");

        verify(monitoringLogService).logSubscriptionServiceCallFailure(queryIds, null, null, "MESSAGE_TEXT", null);
    }

    private Map<String, String> createOrgNoHsaIdMap(int count) {
        final var orgNoHsaIdMap = new HashMap<String, String>();
        for (var i = 1; i <= count; i++) {
            orgNoHsaIdMap.put("ORG_NO_" + i, "HSA_ID_" + i);
        }
        return orgNoHsaIdMap;
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

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(LIST_ORGANIZATION_RESPONSE)))
            .thenReturn(new ResponseEntity<>(orgList, httpStatus));
    }

    private void setMockToReturnRestClientException() {
        final var e = new RestClientException("MESSAGE_TEXT");
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(LIST_ORGANIZATION_RESPONSE)))
            .thenThrow(e);
    }

    private void setMockToReturnRestClientResponseException() {
        final var e = new RestClientResponseException("MESSAGE_TEXT", 777, "statusText", null,null, null);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(LIST_ORGANIZATION_RESPONSE)))
            .thenThrow(e);
    }

    private void setMockToReturnHttpServerErrorException(HttpStatus httpStatus) {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.setDate(ZonedDateTime.of(LOCAL_DATE_TIME, ZoneId.of("Europe/Stockholm")));
        final var e = new HttpServerErrorException("MESSAGE_TEXT", httpStatus, httpStatus.name(), httpHeaders, null, StandardCharsets.UTF_8);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(LIST_ORGANIZATION_RESPONSE)))
            .thenThrow(e);
    }

    private void setMockToReturnHttpClientErrorException(HttpStatus httpStatus) {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.setDate(ZonedDateTime.of(LOCAL_DATE_TIME, ZoneId.of("Europe/Stockholm")));
        final var e = new HttpClientErrorException("MESSAGE_TEXT", httpStatus, httpStatus.name(), httpHeaders, null, StandardCharsets.UTF_8);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(LIST_ORGANIZATION_RESPONSE)))
            .thenThrow(e);
    }

    private OrganizationResponse createOrganization(int orgNo, int serviceCodeCount) {
        final var orgResponse = new OrganizationResponse();
        final var serviceCodeList = createServiceCodeList(serviceCodeCount);
        orgResponse.setOrganizationNumber("ORG_NO_" + orgNo);
        orgResponse.setServiceCodes(serviceCodeList);
        return orgResponse;
    }

    private List<String> createServiceCodeList(int serviceCodeCount) {
        final var serviceCodeList = new ArrayList<String>();
        for (var i = 1; i <= serviceCodeCount; i++) {
            serviceCodeList.add("SERVICE_CODE_" + i);
        }
        return serviceCodeList;
    }
}
