/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationRequest;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationResponse;

@Service
public class SubscriptionRestClient {

  @Value("${servicenow.username}")
  private String serviceNowUsername;

  @Value("${servicenow.password}")
  private String serviceNowPassword;

  @Value("${servicenow.subscription.url.v2}")
  private String serviceNowSubscriptionServiceUrl;

  @Value("#{${servicenow.subscription.service.names}}")
  private List<String> serviceNowSubscriptionServiceNames;

  private final RestClient serviceNowRestClient;

  public SubscriptionRestClient(
      @Qualifier("serviceNowRestClient") RestClient serviceNowRestClient) {
    this.serviceNowRestClient = serviceNowRestClient;
  }

  public OrganizationResponse getSubscriptionServiceResponse(Set<String> organizationNumbers) {
    final var response =
        serviceNowRestClient
            .post()
            .uri(serviceNowSubscriptionServiceUrl)
            .headers(h -> h.setBasicAuth(serviceNowUsername, serviceNowPassword))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(buildRequest(organizationNumbers))
            .retrieve()
            .body(OrganizationResponse.class);

    if (response == null) {
      throw new IllegalStateException("Response body was null");
    }
    return response;
  }

  private OrganizationRequest buildRequest(Set<String> organizationNumbers) {
    return OrganizationRequest.builder()
        .services(serviceNowSubscriptionServiceNames)
        .customers(new ArrayList<>(organizationNumbers))
        .build();
  }
}
