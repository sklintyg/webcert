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

import se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum;
import se.inera.intyg.webcert.integration.api.subscription.SubscriptionRestService;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationRequest;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Profile(SERVICENOW_INTEGRATION_PROFILE)
public class ServiceNowSubscriptionRestServiceImpl implements SubscriptionRestService {

    @Value("${servicenow.username}")
    private String serviceNowUsername;

    @Value("${servicenow.password}")
    private String serviceNowPassword;

    @Value("${servicenow.subscriptions.url}")
    private String serviceNowSubscriptionServiceUrl;

    @Value("${servicenow.subscriptions.service}")
    private String serviceNowSubscriptionService;

    @Value("#{${servicenow.service.codes.eleg}}")
    private List<String> elegServiceCodes;

    @Value("#{${servicenow.service.codes.siths}}")
    private List<String> sithsServiceCodes;

    private final RestTemplate restTemplate;

    public ServiceNowSubscriptionRestServiceImpl(@Qualifier("subscriptionServiceRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<String> getMissingSubscriptions(Map<String, List<String>> organizationNumberHsaIdMap, AuthenticationMethodEnum authMethod) {
        final var organizationResponse = getSubscriptionServiceResponse(organizationNumberHsaIdMap.keySet());
        final var organizationInfo = Objects.requireNonNull(organizationResponse.getBody());
        return getCareProvidersMissingSubscription(organizationInfo, organizationNumberHsaIdMap, authMethod);
    }

    @Override
    public boolean isMissingSubscriptionUnregisteredElegUser(String organizationNumber) {
        final var organizationResponse = getSubscriptionServiceResponse(Set.of(organizationNumber));
        final var organizationInfo = Objects.requireNonNull(organizationResponse.getBody()).getResult();
        return missingSubscription(organizationInfo.get(0).getServiceCodes(), AuthenticationMethodEnum.ELEG);
    }

    private ResponseEntity<OrganizationResponse> getSubscriptionServiceResponse(Set<String> organizationNumbers) {
        final var httpEntity = getRequestEntity(organizationNumbers);
        return restTemplate.exchange(serviceNowSubscriptionServiceUrl, HttpMethod.POST, httpEntity, OrganizationResponse.class);
    }

    private HttpEntity<OrganizationRequest> getRequestEntity(Set<String> organizationNumbers) {
        final var requestBody = OrganizationRequest.builder()
            .service(serviceNowSubscriptionService)
            .customers(new ArrayList<>(organizationNumbers))
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(serviceNowUsername, serviceNowPassword);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(requestBody, headers);
    }

    private List<String> getCareProvidersMissingSubscription(OrganizationResponse organizations,
        Map<String, List<String>> organizationNumberHsaIdMap, AuthenticationMethodEnum authMethod) {
        final var careProvidersMissingSubscription = new ArrayList<String>();

        for (var organization : organizations.getResult()) {
            final var serviceCodes = organization.getServiceCodes();
            if (missingSubscription(serviceCodes, authMethod)) {
                careProvidersMissingSubscription.addAll(organizationNumberHsaIdMap.get(organization.getOrganizationNumber()));
            }
        }
        return careProvidersMissingSubscription;
    }

    private boolean missingSubscription(List<String> activeServiceCodes, AuthenticationMethodEnum authMethod) {
        if (activeServiceCodes.isEmpty()) {
            return true;
        }
        if (authMethod == AuthenticationMethodEnum.ELEG) {
            return activeServiceCodes.stream().noneMatch(serviceCode -> elegServiceCodes.contains(serviceCode));
        }
        return activeServiceCodes.stream().noneMatch(serviceCode -> sithsServiceCodes.contains(serviceCode));
    }
}
