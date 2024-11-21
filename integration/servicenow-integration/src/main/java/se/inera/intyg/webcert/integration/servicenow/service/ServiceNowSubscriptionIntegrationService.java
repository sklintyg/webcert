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
package se.inera.intyg.webcert.integration.servicenow.service;

import static se.inera.intyg.webcert.integration.api.subscription.ServiceNowIntegrationConstants.SERVICENOW_INTEGRATION_PROFILE;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum;
import se.inera.intyg.webcert.integration.api.subscription.SubscriptionIntegrationService;
import se.inera.intyg.webcert.integration.servicenow.client.SubscriptionRestClient;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;

@Service
@RequiredArgsConstructor
@Profile(SERVICENOW_INTEGRATION_PROFILE)
public class ServiceNowSubscriptionIntegrationService implements SubscriptionIntegrationService {

    private final SubscriptionRestClient subscriptionRestClient;
    private final GetCareProvidersMissingSubscriptionService getCareProvidersMissingSubscriptionService;
    private final CheckSubscriptionService checkSubscriptionService;

    @Override
    @PerformanceLogging(eventAction = "get-missing-subscriptions", eventType = MdcLogConstants.EVENT_TYPE_INFO)
    public List<String> getMissingSubscriptions(Map<String, List<String>> organizationNumberHsaIdMap, AuthenticationMethodEnum authMethod) {
        final var organizationResponse = subscriptionRestClient.getSubscriptionServiceResponse(
            organizationNumberHsaIdMap.keySet()
        );
        return getCareProvidersMissingSubscriptionService.get(organizationResponse.getResult(), organizationNumberHsaIdMap, authMethod);
    }

    @Override
    @PerformanceLogging(eventAction = "is-missing-subscriptions-eleg-user", eventType = MdcLogConstants.EVENT_TYPE_INFO)
    public boolean isMissingSubscriptionUnregisteredElegUser(String organizationNumber) {
        final var organizationResponse = subscriptionRestClient.getSubscriptionServiceResponse(
            Set.of(organizationNumber)
        );
        final var serviceCodes = organizationResponse.getResult().get(0).getServiceCodes();
        return checkSubscriptionService.isMissing(serviceCodes, AuthenticationMethodEnum.ELEG);
    }
}
