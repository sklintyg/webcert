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
package se.inera.intyg.webcert.web.config;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.intyg.webcert.infra.security.filter.InternalApiFilter;

/**
 * Adds a second embedded Tomcat connector on {@code internal.api.port} (default 8081).
 *
 * <p>This replicates the two-connector setup from {@code tomcat-gretty.xml}. The existing {@link
 * InternalApiFilter} checks {@code request.getLocalPort()} and blocks requests from the wrong port
 * — that behaviour is unchanged.
 *
 * <p>Spring Boot's management server runs on {@code management.server.port} (8082) as a completely
 * separate {@code TomcatWebServer} instance managed by {@code ManagementServerConfiguration} — it
 * does NOT go through this customizer.
 */
@Configuration
@RequiredArgsConstructor
public class TomcatConfig {

  @Value("${internal.api.port}")
  private int internalPort;

  @Bean
  public WebServerFactoryCustomizer<TomcatServletWebServerFactory>
      internalApiConnectorCustomizer() {
    return factory -> {
      final var connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
      connector.setPort(internalPort);
      factory.addAdditionalTomcatConnectors(connector);
    };
  }
}
