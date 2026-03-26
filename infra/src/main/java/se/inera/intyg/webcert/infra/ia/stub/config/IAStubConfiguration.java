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
package se.inera.intyg.webcert.infra.ia.stub.config;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import se.inera.intyg.webcert.infra.ia.stub.IAStubRestApi;

@Configuration
@Profile({"dev", "ia-stub"})
public class IAStubConfiguration {

  @Autowired private Bus bus;

  @Autowired private JacksonJsonProvider jacksonJsonProvider;

  @Bean
  public IAStubRestApi iaStubRestApi() {
    return new IAStubRestApi();
  }

  @Bean
  public Server iaStubServer(IAStubRestApi iaStubRestApi) {
    JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
    factory.setBus(bus);
    factory.setAddress("/api/ia-api");
    factory.setServiceBeans(Arrays.asList(iaStubRestApi));
    factory.setProviders(Arrays.asList(jacksonJsonProvider));
    Map<Object, Object> extensionMappings = new HashMap<>();
    extensionMappings.put("json", "application/json");
    factory.setExtensionMappings(extensionMappings);
    return factory.create();
  }
}
