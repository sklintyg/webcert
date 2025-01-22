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
package se.inera.intyg.webcert.integration.servicenow.stub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.integration.servicenow.stub.state.ServiceNowStubState;

@ExtendWith(MockitoExtension.class)
class ServiceNowStubSettingsApiServiceTest {

    @Spy
    private ServiceNowStubState stubState;

    @InjectMocks
    private ServiceNowStubSettingsApiService serviceNowStubSettingsApiService;

    @BeforeEach
    void setup() {
        stubState.setSubscriptionReturnValue(true);
        stubState.setHttpErrorCode(0);
        stubState.setActiveSubscriptions(createActiveSubscriptions());
    }

    @Test
    void shouldSetReturnValueAndClearActiveSubscriptions() {
        serviceNowStubSettingsApiService.setSubscriptionReturnValue(false);

        assertFalse(stubState.getSubscriptionReturnValue());
        assertTrue(stubState.getActiveSubscriptions().isEmpty());
    }

    @Test
    void shouldReturnSubscriptionReturnValue() {
        final var response = serviceNowStubSettingsApiService.getSubscriptionReturnValue();
        assertTrue(response);
    }

    @Test
    void shouldAddActiveSubscription() {
        serviceNowStubSettingsApiService.setActiveSubscription("ORGANIZATION_NUMBER_3", "SERVICE_CODE_1");

        assertTrue(stubState.getActiveSubscriptions().containsKey("ORGANIZATION_NUMBER_3"));
        assertEquals("SERVICE_CODE_1", stubState.getActiveSubscriptions().get("ORGANIZATION_NUMBER_3").get(0));
    }

    @Test
    void shouldAddActiveSubscriptionToExistingOrganization() {
        serviceNowStubSettingsApiService.setActiveSubscription("ORGANIZATION_NUMBER_2", "SERVICE_CODE_2");

        assertTrue(stubState.getActiveSubscriptions().containsKey("ORGANIZATION_NUMBER_2"));
        assertEquals(2, stubState.getActiveSubscriptions().get("ORGANIZATION_NUMBER_2").size());
    }

    @Test
    void shouldRemoveActiveSubscriptions() {
        serviceNowStubSettingsApiService.removeActiveSubscriptions("ORGANIZATION_NUMBER_1");

        assertFalse(stubState.getActiveSubscriptions().containsKey("ORGANIZATION_NUMBER_1"));
    }

    @Test
    void shouldClearActiveSubscriptions() {
        stubState.setActiveSubscriptions(createActiveSubscriptions());

        serviceNowStubSettingsApiService.clearActiveSubscriptions();

        assertTrue(stubState.getActiveSubscriptions().isEmpty());
    }

    @Test
    void shouldGetActiveSubscriptions() {
        final var response = serviceNowStubSettingsApiService.getActiveSubscriptions();
        assertEquals(2, response.size());
    }

    @Test
    void shouldCallStubStateWhenSettingErrorCode() {
        serviceNowStubSettingsApiService.setHttpError(403);
        assertEquals(403, stubState.getHttpErrorCode());
    }

    @Test
    void shouldSettubStateErrorCodeToZeroWhenClearingErrorCode() {
        serviceNowStubSettingsApiService.setHttpError(403);
        assertEquals(403, stubState.getHttpErrorCode());

        serviceNowStubSettingsApiService.clearHttpError();
        assertEquals(0, stubState.getHttpErrorCode());
    }

    private Map<String, List<String>> createActiveSubscriptions() {
        final var map = new HashMap<String, List<String>>();
        final var serviceCodes = new ArrayList<String>();
        serviceCodes.add("SERVICE_CODE_1");
        map.put("ORGANIZATION_NUMBER_1", serviceCodes);
        map.put("ORGANIZATION_NUMBER_2", serviceCodes);
        return map;
    }
}
