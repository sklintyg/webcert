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

package se.inera.intyg.webcert.integration.servicenow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum.SITHS;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.integration.servicenow.dto.Organization;

@ExtendWith(MockitoExtension.class)
class GetCareProvidersMissingSubscriptionServiceTest {

    @Mock
    private CheckSubscriptionService checkSubscriptionService;

    @InjectMocks
    private GetCareProvidersMissingSubscriptionService getCareProvidersMissingSubscriptionService;

    private static final String ORGANIZATION_NUMBER_1 = "ORGANIZATION_NUMBER_1";
    private static final String HSA_ID_1 = "HSA_ID_1";
    private static final String HSA_ID_2 = "HSA_ID_2";
    private static final String HSA_ID_3 = "HSA_ID_3";

    @Test
    void shouldAddHsaIdToListWhenMissingSubscription() {
        final var organizations = List.of(
            Organization.builder()
                .organizationNumber(ORGANIZATION_NUMBER_1)
                .serviceCodes(Collections.emptyList())
                .build()
        );

        final var organizationHsaIdMap = Map.of(ORGANIZATION_NUMBER_1, List.of(HSA_ID_1));

        doReturn(true).when(checkSubscriptionService).isMissing(organizations.getFirst().getServiceCodes(),
            SITHS);

        final var actualResponse = getCareProvidersMissingSubscriptionService.get(organizations, organizationHsaIdMap, SITHS);

        assertEquals(1, actualResponse.size());
        assertEquals(HSA_ID_1, actualResponse.getFirst());
    }

    @Test
    void shouldAddMultipleHsaIdToListWhenMissingSubscription() {
        final var organizations = List.of(
            Organization.builder()
                .organizationNumber(ORGANIZATION_NUMBER_1)
                .serviceCodes(Collections.emptyList())
                .build()
        );

        final var organizationHsaIdMap = Map.of(
            ORGANIZATION_NUMBER_1, List.of(HSA_ID_1, HSA_ID_2, HSA_ID_3)
        );

        doReturn(true).when(checkSubscriptionService).isMissing(organizations.getFirst().getServiceCodes(), SITHS);

        final var actualResponse = getCareProvidersMissingSubscriptionService.get(organizations, organizationHsaIdMap, SITHS);

        assertEquals(3, actualResponse.size());
        assertEquals(HSA_ID_1, actualResponse.get(0));
        assertEquals(HSA_ID_2, actualResponse.get(1));
        assertEquals(HSA_ID_3, actualResponse.get(2));
    }

    @Test
    void shouldNotAddHsaIdToListWhenNotMissingSubscription() {
        final var organizations = List.of(
            Organization.builder()
                .organizationNumber(ORGANIZATION_NUMBER_1)
                .serviceCodes(Collections.emptyList())
                .build()
        );

        final var organizationHsaIdMap = Map.of(
            ORGANIZATION_NUMBER_1, List.of(HSA_ID_1, HSA_ID_2, HSA_ID_3)
        );

        doReturn(false).when(checkSubscriptionService).isMissing(organizations.getFirst().getServiceCodes(), SITHS);

        final var actualResponse = getCareProvidersMissingSubscriptionService.get(organizations, organizationHsaIdMap, SITHS);

        assertTrue(actualResponse.isEmpty());
    }
}
