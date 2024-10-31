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

package se.inera.intyg.webcert.integration.servicenow.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationRequest;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationResponse;

@Service
public class SubscriptionRestClient {

    @Value("${servicenow.username}")
    private String serviceNowUsername;

    @Value("${servicenow.password}")
    private String serviceNowPassword;

    @Value("${servicenow.subscription.url}")
    private String serviceNowSubscriptionServiceUrl;

    @Value("${servicenow.subscription.service.name}")
    private String serviceNowSubscriptionServiceName;

    private final RestTemplate serviceNowRestTemplate;

    public SubscriptionRestClient(@Qualifier("serviceNowRestTemplate") RestTemplate serviceNowRestTemplate) {
        this.serviceNowRestTemplate = serviceNowRestTemplate;
    }

    public OrganizationResponse getSubscriptionServiceResponse(Set<String> organizationNumbers) {
        final var httpEntity = getRequestEntity(organizationNumbers);
        final var response = serviceNowRestTemplate.exchange(serviceNowSubscriptionServiceUrl, HttpMethod.POST, httpEntity,
            OrganizationResponse.class);

        if (response.getBody() == null) {
            throw new IllegalStateException("Response body was null");
        }

        return response.getBody();
    }

    private HttpEntity<OrganizationRequest> getRequestEntity(Set<String> organizationNumbers) {
        final var requestBody = OrganizationRequest.builder()
            .service(serviceNowSubscriptionServiceName)
            .customers(new ArrayList<>(organizationNumbers))
            .build();

        final var headers = new HttpHeaders();
        headers.setBasicAuth(serviceNowUsername, serviceNowPassword);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(requestBody, headers);
    }
}
