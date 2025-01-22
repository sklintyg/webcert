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
package se.inera.intyg.webcert.integration.kundportalen.stub.service;

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
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.integration.kundportalen.stub.state.KundportalenStubState;

@RunWith(MockitoJUnitRunner.class)
public class KundportalenStubSettingsApiServiceTest {

    @Spy
    private KundportalenStubState stubState;

    @InjectMocks
    private KundportalenStubSettingsApiService kundportalenStubSettingsApiService;

    @Test
    public void shouldSetReturnValueAndClearActiveSubscriptions() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        kundportalenStubSettingsApiService.setSubscriptionReturnValue(true);
        verify(stubState, times(1)).setSubscriptionReturnValue(true);
        assertTrue(activeSubscriptions.isEmpty());
    }

    @Test
    public void testGetSubscriptionReturnValue() {
        final var response = kundportalenStubSettingsApiService.getSubscriptionReturnValue();
        assertTrue(response);
    }

    @Test
    public void testGetActiveSubscription() {
        final var activeSubscriptions = createActiveSubscriptions();

        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);
        kundportalenStubSettingsApiService.setActiveSubscription("ORGANIZATION_NUMBER_3", "SERVICE_CODE_1");

        assertTrue(activeSubscriptions.containsKey("ORGANIZATION_NUMBER_3"));
        assertEquals("SERVICE_CODE_1", activeSubscriptions.get("ORGANIZATION_NUMBER_3").get(0));
    }

    @Test
    public void testAddActiveSubscriptionToExistingOrganization() {
        final var activeSubscriptions = createActiveSubscriptions();
        final var serviceCodes = new ArrayList<String>();
        serviceCodes.add("SERVICE_CODE_1");
        activeSubscriptions.put("ORGANIZATION_NUMBER_2", serviceCodes);

        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);
        kundportalenStubSettingsApiService.setActiveSubscription("ORGANIZATION_NUMBER_2", "SERVICE_CODE_2");

        assertTrue(activeSubscriptions.containsKey("ORGANIZATION_NUMBER_2"));
        assertEquals(2, activeSubscriptions.get("ORGANIZATION_NUMBER_2").size());
    }

    @Test
    public void testRemoveActiveSubscriptions() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        kundportalenStubSettingsApiService.removeActiveSubscriptions("ORGANIZATION_NUMBER_1");

        assertFalse(activeSubscriptions.containsKey("ORGANIZATION_NUMBER_1"));
    }

    @Test
    public void testClearActiveSubscriptions() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        kundportalenStubSettingsApiService.clearActiveSubscriptions();

        assertTrue(activeSubscriptions.isEmpty());
    }

    @Test
    public void testGetActiveSubscriptions() {
        final var activeSubscriptions = createActiveSubscriptions();
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        final var response = kundportalenStubSettingsApiService.getActiveSubscriptions();

        assertEquals(2, response.size());
    }

    @Test
    public void shouldCallStubStateWhenSettingErrorCode() {
        kundportalenStubSettingsApiService.setHttpError(403);
        verify(stubState, times(1)).setHttpErrorCode(403);
    }

    @Test
    public void shouldCallStubStateWithMinusOneWhenClearingErrorCode() {
        kundportalenStubSettingsApiService.clearHttpError();
        verify(stubState, times(1)).setHttpErrorCode(0);
    }

    private Map<String, List<String>> createActiveSubscriptions() {
        final var map = new HashMap<String, List<String>>();
        map.put("ORGANIZATION_NUMBER_1", List.of("SERVICE_CODE_1"));
        map.put("ORGANIZATION_NUMBER_2", List.of("SERVICE_CODE_1"));
        return map;
    }
}
