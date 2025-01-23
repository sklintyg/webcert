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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import se.inera.intyg.webcert.integration.servicenow.stub.api.ServiceNowStubRestApiV2;
import se.inera.intyg.webcert.integration.servicenow.stub.api.ServiceNowStubSettingsApi;

@Configuration
@RequiredArgsConstructor
@Profile("(dev | wc-all-stubs | servicenow-integration-stub-v2) & !wc-kundportalen-stub")
@ComponentScan(basePackages = "se.inera.intyg.webcert.integration.servicenow.stub")
public class ServiceNowStubConfigV2 {

    private final ServiceNowStubRestApiV2 servicenowStubRestApiV2;
    private final ServiceNowStubSettingsApi servicenowStubSettingsApi;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        final var propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource("application.properties"));
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public Server server() {
        List<JacksonJsonProvider> providers = new ArrayList<>();
        providers.add(getJsonProvider());

        JAXRSServerFactoryBean endpoint = new JAXRSServerFactoryBean();
        endpoint.setProviders(providers);
        endpoint.setBus(springBus());
        endpoint.setAddress("/stubs/servicenowstub");
        endpoint.setServiceBeans(Arrays.asList(servicenowStubRestApiV2, servicenowStubSettingsApi));

        return endpoint.create();
    }

    @Bean
    public JacksonJsonProvider getJsonProvider() {
        return new JacksonJsonProvider();
    }

    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {
        return new SpringBus();
    }
}
