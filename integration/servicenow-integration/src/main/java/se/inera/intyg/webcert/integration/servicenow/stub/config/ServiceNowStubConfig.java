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
package se.inera.intyg.webcert.integration.servicenow.stub.config;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import se.inera.intyg.webcert.integration.servicenow.stub.api.ServiceNowStubRestApi;
import se.inera.intyg.webcert.integration.servicenow.stub.settings.api.ServiceNowStubSettingsApi;

@Configuration
@Slf4j
@RequiredArgsConstructor
@Profile("(dev | wc-all-stubs | servicenow-integration-stub-v2) & !servicenow-integration-stub")
@ComponentScan(basePackages = {
    "se.inera.intyg.webcert.integration.servicenow.stub",
    "se.inera.intyg.webcert.integration.servicenow.stub.settings"})
public class ServiceNowStubConfig {

    private final ServiceNowStubRestApi servicenowStubRestApi;
    private final ServiceNowStubSettingsApi servicenowStubSettingsApi;

    @Bean
    public Server server(JacksonJsonProvider jacksonJsonProvider, SpringBus springBus) {
        final var providers = List.of(jacksonJsonProvider);
        final var endpoint = new JAXRSServerFactoryBean();
        endpoint.setProviders(providers);
        endpoint.setBus(springBus);
        endpoint.setAddress("/stubs/servicenowstub");
        endpoint.setServiceBeans(List.of(servicenowStubRestApi, servicenowStubSettingsApi));
        log.info("Activating servicenow-integration-v2-stub for subscription queries");
        return endpoint.create();
    }

}
