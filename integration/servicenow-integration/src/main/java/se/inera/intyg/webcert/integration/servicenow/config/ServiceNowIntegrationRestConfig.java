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
package se.inera.intyg.webcert.integration.servicenow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ServiceNowIntegrationRestConfig {

  @Value("${servicenow.connection.request.timeout}")
  private int connectionRequestTimeout;

  @Value("${servicenow.read.timeout}")
  private int readTimeout;

  private static final String SUBSCRIPTION_REST_CLIENT = "serviceNowRestClient";

  @Bean(SUBSCRIPTION_REST_CLIENT)
  RestClient restClient(RestClient.Builder client) {
    final var factory = new HttpComponentsClientHttpRequestFactory();
    factory.setConnectionRequestTimeout(connectionRequestTimeout);
    factory.setReadTimeout(readTimeout);

    return client.requestFactory(factory).build();
  }
}
