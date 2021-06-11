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

package se.inera.intyg.webcert.kundportalenstub.service;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.kundportalenstub.state.KundportalenStubState;

@RunWith(MockitoJUnitRunner.class)
public class KundportalenStubRestApiServiceTest {

    @Mock
    private KundportalenStubState stubState;

    @InjectMocks
    KundportalenStubRestApiService kundportalenStubRestApiService;

    @Test
    public void shouldReturnMapWithOneActiveServiceCodeWhenOneSubscriptionExists() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        final var response = kundportalenStubRestApiService.getSubscriptionInfo(List.of("ORGANIZATION_NUMBER_1"));

        final var serviceCodeList = castToListString(String.class, (List<?>) response.get(0).get("service_code_subscriptions"));
        assertEquals(1, response.size());
        assertEquals("ORGANIZATION_NUMBER_1", response.get(0).get("org_no"));
        assertTrue(serviceCodeList.contains("SERVICE_CODE_1"));
    }

    @Test
    public void shouldReturnMapWithTwoActiveServiceCodesWhenTwoSubscriptionsExists() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        final var response = kundportalenStubRestApiService.getSubscriptionInfo(List.of("ORGANIZATION_NUMBER_2"));

        final var serviceCodeList = castToListString(String.class, (List<?>) response.get(0).get("service_code_subscriptions"));
        assertEquals(1, response.size());
        assertEquals("ORGANIZATION_NUMBER_2", response.get(0).get("org_no"));
        assertTrue(serviceCodeList.contains("SERVICE_CODE_1"));
        assertTrue(serviceCodeList.contains("SERVICE_CODE_2"));
    }

    @Test
    public void shouldReturnEmptyListWhenNoSubscriptionExists() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        final var response = kundportalenStubRestApiService.getSubscriptionInfo(List.of("ORGANIZATION_NUMBER_3"));

        assertEquals(1, response.size());
        assertEquals("ORGANIZATION_NUMBER_3", response.get(0).get("org_no"));
        assertTrue(((List<?>) response.get(0).get("service_code_subscriptions")).isEmpty());
    }

    @Test
    public void shouldReturnTwoOrganizationsWithServiceeCodesWhenBothHaveSubscription() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        final var response = kundportalenStubRestApiService.getSubscriptionInfo(List.of("ORGANIZATION_NUMBER_1",
            "ORGANIZATION_NUMBER_2"));

        final var serviceCodeList1 = castToListString(String.class, (List<?>) response.get(0).get("service_code_subscriptions"));
        final var serviceCodeList2 = castToListString(String.class, (List<?>) response.get(1).get("service_code_subscriptions"));
        assertEquals(2, response.size());
        assertEquals("ORGANIZATION_NUMBER_1", response.get(0).get("org_no"));
        assertEquals("ORGANIZATION_NUMBER_2", response.get(1).get("org_no"));
        assertEquals(1, serviceCodeList1.size());
        assertEquals(2, serviceCodeList2.size());
    }

    @Test
    public void shouldReturnTwoOrganizationsWhenOneHaveSubscription() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        final var response = kundportalenStubRestApiService.getSubscriptionInfo(List.of("ORGANIZATION_NUMBER_2",
            "ORGANIZATION_NUMBER_3"));

        assertEquals(2, response.size());
        assertEquals("ORGANIZATION_NUMBER_2", response.get(0).get("org_no"));
        assertEquals("ORGANIZATION_NUMBER_3", response.get(1).get("org_no"));
        assertEquals(2, ((List<?>) response.get(0).get("service_code_subscriptions")).size());
        assertTrue(((List<?>) response.get(1).get("service_code_subscriptions")).isEmpty());
    }

    @Test
    public void shouldReturnAllServiceCodesWhenActiveSubcriptionsIsEmptyAndReturnValueIsTrue() {
        final var activeSubscriptions = new HashMap<String, List<String>>();
        final var setReturnValue = true;
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);
        when(stubState.getSubscriptionReturnValue()).thenReturn(setReturnValue);
        when(stubState.getServiceCodeList()).thenReturn(List.of("SERVICE_CODE_1", "SERVICE_CODE_2", "SERVICE_CODE_3"));

        final var response = kundportalenStubRestApiService.getSubscriptionInfo(List.of("ORGANIZATION_NUMBER_1"));

        assertEquals(1, response.size());
        assertEquals(3, ((List<?>) response.get(0).get("service_code_subscriptions")).size());
    }

    @Test
    public void shouldReturnNoServiceCodesWhenActiveSubcriptionsIsEmptyAndReturnValueIsFalse() {
        final var activeSubscriptions = new HashMap<String, List<String>>();
        final var setReturnValue = false;
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);
        when(stubState.getSubscriptionReturnValue()).thenReturn(setReturnValue);

        final var response = kundportalenStubRestApiService.getSubscriptionInfo(List.of("ORGANIZATION_NUMBER_1"));

        assertEquals(1, response.size());
        assertEquals(0, ((List<?>) response.get(0).get("service_code_subscriptions")).size());
    }

    private Map<String, List<String>> createActiveSubscriptions() {
        return Map.of(
            "ORGANIZATION_NUMBER_1", List.of("SERVICE_CODE_1"),
            "ORGANIZATION_NUMBER_2", List.of("SERVICE_CODE_1", "SERVICE_CODE_2")
        );
    }

    private <T> List<T> castToListString(Class<T> clazz, List<?> collection)
        throws ClassCastException {
        List<T> result = new ArrayList<>(collection.size());
        for (Object o : collection) {
            result.add(clazz.cast(o));
        }
        return result;
    }
}
