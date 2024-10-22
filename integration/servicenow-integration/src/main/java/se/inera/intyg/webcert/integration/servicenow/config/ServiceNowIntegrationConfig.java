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
package se.inera.intyg.webcert.integration.servicenow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.webcert.integration.api.subscription.ServiceNowIntegrationConstants;

@Configuration
@Profile(ServiceNowIntegrationConstants.SERVICENOW_INTEGRATION_PROFILE)
@ComponentScan(basePackages = {"se.inera.intyg.webcert.integration.servicenow.client", "se.inera.intyg.webcert.integration.servicenow.dto",
    "se.inera.intyg.webcert.integration.servicenow.service"})
public class ServiceNowIntegrationConfig {

    @Value("${servicenow.connection.request.timeout}")
    private int connectionRequestTimeout;

    @Value("${servicenow.connection.timeout}")
    private int connectionTimeout;

    @Value("${servicenow.read.timeout}")
    private int readTimeout;

    private static final String SUBSCRIPTION_SERVICE_REST_TEMPLATE = "serviceNowRestTemplate";

    @Bean(SUBSCRIPTION_SERVICE_REST_TEMPLATE)
    RestTemplate restTemplate() {
        final var httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(connectionRequestTimeout);
        httpRequestFactory.setConnectTimeout(connectionTimeout);
        return new RestTemplate(httpRequestFactory);
    }
}
