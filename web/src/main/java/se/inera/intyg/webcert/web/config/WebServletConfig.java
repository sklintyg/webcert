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

import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet;
import lombok.RequiredArgsConstructor;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class WebServletConfig {

  private final MetricsServlet metricsServlet;

  @Bean
  public ServletRegistrationBean<CXFServlet> cxfServlet() {
    var registration = new ServletRegistrationBean<>(new CXFServlet(), "/services/*");
    registration.setName("services");
    registration.setLoadOnStartup(1);
    return registration;
  }

  @Bean
  public ServletRegistrationBean<MetricsServlet> metricsServletRegistrationBean() {
    var registration = new ServletRegistrationBean<>(metricsServlet, "/metrics");
    registration.setName("metrics");
    return registration;
  }
}