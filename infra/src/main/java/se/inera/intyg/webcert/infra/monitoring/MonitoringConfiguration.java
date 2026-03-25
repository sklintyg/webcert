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
package se.inera.intyg.webcert.infra.monitoring;

import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import se.inera.intyg.webcert.infra.monitoring.annotation.EnablePrometheusTiming;
import se.inera.intyg.webcert.infra.monitoring.logging.LogMDCHelper;
import se.inera.intyg.webcert.infra.monitoring.logging.LogMDCServletFilter;

@Configuration
@EnablePrometheusTiming
@EnableAspectJAutoProxy
public class MonitoringConfiguration {

  public MonitoringConfiguration() {
    DefaultExports.initialize();
  }

  @Bean
  public MetricsServlet metricsServlet() {
    return new MetricsServlet();
  }

  @Bean
  public LogMDCServletFilter logMDCServletFilter() {
    return new LogMDCServletFilter();
  }

  @Bean
  public LogMDCHelper logMDCHelper() {
    return new LogMDCHelper();
  }
}
