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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    public void shouldReturnTrueWhenSubscriptionExists() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        final var response = kundportalenStubRestApiService.getSubscriptionInfo("ORGANIZATION_NUMBER_1", "SERVICE_CODE_1");

        assertTrue(response);
    }

    @Test
    public void shouldReturnFalseWhenNoSubscriptionExists() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        final var response = kundportalenStubRestApiService.getSubscriptionInfo("ORGANIZATION_NUMBER_3", "SERVICE_CODE_1");

        assertFalse(response);
    }

    @Test
    public void shouldReturnSetReturnValueWhenActiveSubcriptionsIsEmpty() {
        final var activeSubscriptions = new HashMap<String, String>();
        final var setReturnValue = true;
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);
        when(stubState.getSubscriptionReturnValue()).thenReturn(setReturnValue);

        final var response = kundportalenStubRestApiService.getSubscriptionInfo("ORGANIZATION_NUMBER_2", "SERVICE_CODE_1");

        assertEquals(response, setReturnValue);
    }

    @Test
    public void shouldCallStubStateGetServices() {
        final var serviceList = createServiceList();
        when(stubState.getServices()).thenReturn(serviceList);
        kundportalenStubRestApiService.getServices();
        verify(stubState, times(1)).getServices();
    }

    private Map<String, String> createActiveSubscriptions() {
        return Map.of(
            "ORGANIZATION_NUMBER_1", "SERVICE_CODE_1",
            "ORGANIZATION_NUMBER_2", "SERVICE_CODE_1");
    }

    private List<Map<String, String>> createServiceList() {
        final var list = new ArrayList<Map<String, String>>();
        list.add(Map.of("ORGANIZATION_NUMBER_1", "SERVICE_CODE_1"));
        list.add(Map.of("ORGANIZATION_NUMBER_2", "SERVICE_CODE_1"));
        return list;
    }
}
