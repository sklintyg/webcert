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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationResponse;

@Service
public class GetCareProvidersMissingSubscriptionService {

    private final MissingSubscriptionService missingSubscriptionService;

    public GetCareProvidersMissingSubscriptionService(MissingSubscriptionService missingSubscriptionService) {
        this.missingSubscriptionService = missingSubscriptionService;
    }

    public List<String> get(OrganizationResponse organizations,
        Map<String, List<String>> organizationNumberHsaIdMap, AuthenticationMethodEnum authMethod) {
        final var careProvidersMissingSubscription = new ArrayList<String>();

        for (var organization : organizations.getResult()) {
            final var serviceCodes = organization.getServiceCodes();
            if (missingSubscriptionService.missingSubscription(serviceCodes, authMethod)) {
                careProvidersMissingSubscription.addAll(organizationNumberHsaIdMap.get(organization.getOrganizationNumber()));
            }
        }
        return careProvidersMissingSubscription;
    }


}
