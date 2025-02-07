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

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum;
import se.inera.intyg.webcert.integration.api.subscription.SubscriptionIntegrationService;
import se.inera.intyg.webcert.integration.servicenow.service.CheckSubscriptionService;
import se.inera.intyg.webcert.integration.servicenow.service.GetCareProvidersMissingSubscriptionService;
import se.inera.intyg.webcert.integration.servicenow.v2.client.SubscriptionRestClientV2;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;

@Service
@RequiredArgsConstructor
public class ServiceNowSubscriptionIntegrationServiceV2 implements SubscriptionIntegrationService {

    private final SubscriptionRestClientV2 subscriptionRestClientV2;
    private final GetCareProvidersMissingSubscriptionService getCareProvidersMissingSubscriptionService;
    private final CheckSubscriptionService checkSubscriptionService;

    @Override
    @PerformanceLogging(eventAction = "get-missing-subscriptions", eventType = MdcLogConstants.EVENT_TYPE_INFO)
    public List<String> getMissingSubscriptions(Map<String, List<String>> organizationNumberHsaIdMap, AuthenticationMethodEnum authMethod) {
        final var organizationResponse = subscriptionRestClientV2.getSubscriptionServiceResponse(
            organizationNumberHsaIdMap.keySet()
        );
        return getCareProvidersMissingSubscriptionService.get(organizationResponse.getResult(), organizationNumberHsaIdMap, authMethod);
    }

    @Override
    @PerformanceLogging(eventAction = "is-missing-subscriptions-eleg-user", eventType = MdcLogConstants.EVENT_TYPE_INFO)
    public boolean isMissingSubscriptionUnregisteredElegUser(String organizationNumber) {
        final var organizationResponse = subscriptionRestClientV2.getSubscriptionServiceResponse(
            Set.of(organizationNumber)
        );
        final var serviceCodes = organizationResponse.getResult().getFirst().getServiceCodes();
        return checkSubscriptionService.isMissing(serviceCodes, AuthenticationMethodEnum.ELEG);
    }
}
