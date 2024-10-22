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
package se.inera.intyg.webcert.integration.kundportalen.stub.service;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.integration.kundportalen.stub.state.KundportalenStubState;

@RunWith(MockitoJUnitRunner.class)
public class KundportalenStubRestApiServiceTest {

    @Mock
    private KundportalenStubState stubState;

    @InjectMocks
    KundportalenStubRestApiService kundportalenStubRestApiService;

    private static final String ACCESS_TOKEN = "accessToken";
    private static final String SERVICE = "Intygstjänster";

    @Test
    public void shouldReturnMapWithOneActiveServiceCodeWhenOneSubscriptionExists() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getHttpErrorCode()).thenReturn(0);
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        final var response = kundportalenStubRestApiService.createSubscriptionResponse(ACCESS_TOKEN, SERVICE,
            List.of("ORGANIZATION_NUMBER_1")).readEntity(new GenericType<List<Map<String, Object>>>() {
        });

        final var serviceCodeList = objectToListOfStrings(response.get(0).get("serviceCode"));
        assertEquals(1, response.size());
        assertEquals("ORGANIZATION_NUMBER_1", response.get(0).get("orgNo"));
        assertTrue(serviceCodeList.contains("SERVICE_CODE_1"));
    }

    @Test
    public void shouldReturnMapWithTwoActiveServiceCodesWhenTwoSubscriptionsExists() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getHttpErrorCode()).thenReturn(0);
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        final var response = kundportalenStubRestApiService.createSubscriptionResponse(ACCESS_TOKEN, SERVICE,
            List.of("ORGANIZATION_NUMBER_2")).readEntity(new GenericType<List<Map<String, Object>>>() {
        });

        final var serviceCodeList = objectToListOfStrings(response.get(0).get("serviceCode"));
        assertEquals(1, response.size());
        assertEquals("ORGANIZATION_NUMBER_2", response.get(0).get("orgNo"));
        assertTrue(serviceCodeList.contains("SERVICE_CODE_1"));
        assertTrue(serviceCodeList.contains("SERVICE_CODE_2"));
    }

    @Test
    public void shouldReturnEmptyListWhenNoSubscriptionExists() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getHttpErrorCode()).thenReturn(0);
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        final var response = kundportalenStubRestApiService.createSubscriptionResponse(ACCESS_TOKEN, SERVICE,
            List.of("ORGANIZATION_NUMBER_3")).readEntity(new GenericType<List<Map<String, Object>>>() {
        });

        assertEquals(1, response.size());
        assertEquals("ORGANIZATION_NUMBER_3", response.get(0).get("orgNo"));
        assertTrue(((List<?>) response.get(0).get("serviceCode")).isEmpty());
    }

    @Test
    public void shouldReturnTwoOrganizationsWithServiceeCodesWhenBothHaveSubscription() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getHttpErrorCode()).thenReturn(0);
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        final var response = kundportalenStubRestApiService.createSubscriptionResponse(ACCESS_TOKEN, SERVICE,
            List.of("ORGANIZATION_NUMBER_1", "ORGANIZATION_NUMBER_2")).readEntity(new GenericType<List<Map<String, Object>>>() {
        });

        assert response != null;
        final var serviceCodeList1 = objectToListOfStrings(response.get(0).get("serviceCode"));
        final var serviceCodeList2 = objectToListOfStrings(response.get(1).get("serviceCode"));
        assertEquals(2, response.size());
        assertEquals("ORGANIZATION_NUMBER_1", response.get(0).get("orgNo"));
        assertEquals("ORGANIZATION_NUMBER_2", response.get(1).get("orgNo"));
        assertEquals(1, serviceCodeList1.size());
        assertEquals(2, serviceCodeList2.size());
    }

    @Test
    public void shouldReturnTwoOrganizationsWhenOneHaveSubscription() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getHttpErrorCode()).thenReturn(0);
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        final var response = kundportalenStubRestApiService.createSubscriptionResponse(ACCESS_TOKEN, SERVICE,
            List.of("ORGANIZATION_NUMBER_2", "ORGANIZATION_NUMBER_3")).readEntity(new GenericType<List<Map<String, Object>>>() {
        });

        assert response != null;
        assertEquals(2, response.size());
        assertEquals("ORGANIZATION_NUMBER_2", response.get(0).get("orgNo"));
        assertEquals("ORGANIZATION_NUMBER_3", response.get(1).get("orgNo"));
        assertEquals(2, ((List<?>) response.get(0).get("serviceCode")).size());
        assertTrue(((List<?>) response.get(1).get("serviceCode")).isEmpty());
    }

    @Test
    public void shouldReturnAllServiceCodesWhenActiveSubcriptionsIsEmptyAndReturnValueIsTrue() {
        final var activeSubscriptions = new HashMap<String, List<String>>();
        final var setReturnValue = true;
        when(stubState.getHttpErrorCode()).thenReturn(0);
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);
        when(stubState.getSubscriptionReturnValue()).thenReturn(setReturnValue);
        when(stubState.getServiceCodeList()).thenReturn(List.of("SERVICE_CODE_1", "SERVICE_CODE_2", "SERVICE_CODE_3"));

        final var response = kundportalenStubRestApiService.createSubscriptionResponse(ACCESS_TOKEN, SERVICE,
            List.of("ORGANIZATION_NUMBER_1")).readEntity(new GenericType<List<Map<String, Object>>>() {
        });

        assertEquals(1, response.size());
        assertEquals(3, ((List<?>) response.get(0).get("serviceCode")).size());
    }

    @Test
    public void shouldReturnNoServiceCodesWhenActiveSubcriptionsIsEmptyAndReturnValueIsFalse() {
        final var activeSubscriptions = new HashMap<String, List<String>>();
        final var setReturnValue = false;
        when(stubState.getHttpErrorCode()).thenReturn(0);
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);
        when(stubState.getSubscriptionReturnValue()).thenReturn(setReturnValue);

        final var response = kundportalenStubRestApiService.createSubscriptionResponse(ACCESS_TOKEN, SERVICE,
            List.of("ORGANIZATION_NUMBER_1")).readEntity(new GenericType<List<Map<String, Object>>>() {
        });

        assertEquals(1, response.size());
        assertEquals(0, ((List<?>) response.get(0).get("serviceCode")).size());
    }

    @Test
    public void shouldReturnNullErrorResponseWhenErrorSet() {
        when(stubState.getHttpErrorCode()).thenReturn(403);

        final var response = kundportalenStubRestApiService.createSubscriptionResponse(ACCESS_TOKEN, SERVICE,
            List.of("ORGANIZATION_NUMBER_1"));
        assertEquals(Status.FORBIDDEN, response.getStatusInfo().toEnum());
    }

    @Test
    public void shouldReturnError500WhenHttpErrorSetToUnknown() {
        when(stubState.getHttpErrorCode()).thenReturn(777);

        final var response = kundportalenStubRestApiService.createSubscriptionResponse(ACCESS_TOKEN, SERVICE,
            List.of("ORGANIZATION_NUMBER_1"));

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatusInfo().toEnum());
    }

    @Test
    public void shouldReturnBadRequestIfNoAuthorizationHeader() {
        final var response = kundportalenStubRestApiService.createSubscriptionResponse(null, SERVICE,
            List.of("ORGANIZATION_NUMBER_1"));

        assertEquals(Status.BAD_REQUEST, response.getStatusInfo().toEnum());
    }

    @Test
    public void shouldReturnBadRequestIfNoServiceQueryParameter() {
        final var response = kundportalenStubRestApiService.createSubscriptionResponse(ACCESS_TOKEN, null,
            List.of("ORGANIZATION_NUMBER_1"));

        assertEquals(Status.BAD_REQUEST, response.getStatusInfo().toEnum());
    }

    private Map<String, List<String>> createActiveSubscriptions() {
        return Map.of(
            "ORGANIZATION_NUMBER_1", List.of("SERVICE_CODE_1"),
            "ORGANIZATION_NUMBER_2", List.of("SERVICE_CODE_1", "SERVICE_CODE_2")
        );
    }

    private List<String> objectToListOfStrings(Object obj) {
        return obj instanceof List<?>
            ? ((List<?>) obj).stream().map(this::objectToString).filter(Objects::nonNull).collect(Collectors.toList()) : null;
    }

    private String objectToString(Object obj) {
        return obj instanceof String ? (String) obj : null;
    }
}
