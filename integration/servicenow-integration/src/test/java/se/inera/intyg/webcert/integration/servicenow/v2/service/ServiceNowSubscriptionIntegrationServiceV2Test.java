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

package se.inera.intyg.webcert.integration.servicenow.v2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum.ELEG;
import static se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum.SITHS;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.integration.servicenow.dto.Organization;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationResponse;
import se.inera.intyg.webcert.integration.servicenow.service.CheckSubscriptionService;
import se.inera.intyg.webcert.integration.servicenow.service.GetCareProvidersMissingSubscriptionService;
import se.inera.intyg.webcert.integration.servicenow.v2.client.SubscriptionRestClientV2;

@ExtendWith(MockitoExtension.class)
class ServiceNowSubscriptionIntegrationServiceV2Test {

    @Mock
    private SubscriptionRestClientV2 subscriptionRestClientV2;
    @Mock
    private GetCareProvidersMissingSubscriptionService getCareProvidersMissingSubscriptionService;
    @Mock
    private CheckSubscriptionService checkSubscriptionService;

    @InjectMocks
    private ServiceNowSubscriptionIntegrationServiceV2 serviceNowSubscriptionIntegrationServiceV2;

    private static final String HSA_ID = "hsaId";
    private static final String ORG_NUMBER = "org_number";

    @Nested
    class GetMissingSubscriptions {

        @Test
        void shouldReturnListOfMissingSubscriptions() {
            final var expectedResult = List.of("expectedResult");
            final var organizationNumberHsaIdMap = Map.of(ORG_NUMBER, List.of(HSA_ID));
            final var organizationResponse = OrganizationResponse.builder().build();

            doReturn(organizationResponse).when(subscriptionRestClientV2).getSubscriptionServiceResponse(
                organizationNumberHsaIdMap.keySet()
            );
            doReturn(expectedResult).when(getCareProvidersMissingSubscriptionService)
                .get(organizationResponse.getResult(), organizationNumberHsaIdMap, SITHS);

            final var actualResult = serviceNowSubscriptionIntegrationServiceV2.getMissingSubscriptions(organizationNumberHsaIdMap, SITHS);
            assertEquals(expectedResult, actualResult);
        }
    }

    @Nested
    class IsMissingSubscriptionUnregisteredElegUser {

        private final OrganizationResponse organizationResponse = OrganizationResponse.builder()
            .result(List.of(
                    Organization.builder()
                        .serviceCodes(Collections.emptyList())
                        .build()
                )
            )
            .build();

        @Test
        void shouldReturnTrueIfSubscriptionIsMissing() {
            doReturn(organizationResponse).when(subscriptionRestClientV2).getSubscriptionServiceResponse(
                Set.of(ORG_NUMBER)
            );
            doReturn(true).when(checkSubscriptionService).isMissing(
                organizationResponse.getResult().getFirst().getServiceCodes(), ELEG);

            assertTrue(serviceNowSubscriptionIntegrationServiceV2.isMissingSubscriptionUnregisteredElegUser(ORG_NUMBER));
        }

        @Test
        void shouldReturnFalseIfSubscriptionIsNotMissing() {
            doReturn(organizationResponse).when(subscriptionRestClientV2).getSubscriptionServiceResponse(
                Set.of(ORG_NUMBER)
            );
            doReturn(false).when(checkSubscriptionService).isMissing(
                organizationResponse.getResult().getFirst().getServiceCodes(), ELEG);

            assertFalse(serviceNowSubscriptionIntegrationServiceV2.isMissingSubscriptionUnregisteredElegUser(ORG_NUMBER));
        }
    }
}