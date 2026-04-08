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
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import se.inera.intyg.common.support.modules.converter.SummaryConverter;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.common.support.services.BefattningService;
import se.inera.intyg.webcert.web.bootstrap.UtkastBootstrapBean;
import se.inera.intyg.webcert.web.service.util.FragaSvarBootstrapBean;
import se.inera.intyg.webcert.web.service.util.IntegreradeEnheterBootstrapBean;

@Configuration
@DependsOn("dbUpdate")
@RequiredArgsConstructor
@EnableTransactionManagement
@PropertySources({
  @PropertySource(value = "classpath:version.properties", ignoreResourceNotFound = true),
  @PropertySource(
      value = "classpath:webcert-notification-route-params.properties",
      ignoreResourceNotFound = true),
})
@ComponentScan("se.inera.intyg.common")
public class AppConfig implements TransactionManagementConfigurer {

  private final JpaTransactionManager transactionManager;

  @Override
  public PlatformTransactionManager annotationDrivenTransactionManager() {
    return transactionManager;
  }

  @Bean(name = Bus.DEFAULT_BUS_ID)
  public SpringBus springBus() {
    return new SpringBus();
  }

  @Bean
  public IntygModuleRegistry moduleRegistry() {
    final var registry = new IntygModuleRegistryImpl();
    registry.setOrigin(ApplicationOrigin.WEBCERT);
    return registry;
  }

  @Bean
  public BefattningService befattningService() {
    return new BefattningService();
  }

  @Bean
  public SummaryConverter summaryConverter() {
    return new SummaryConverter();
  }

  @Bean
  @Profile("dev")
  public FragaSvarBootstrapBean fragaSvarBootstrapBean() {
    return new FragaSvarBootstrapBean();
  }

  @Bean
  @Profile("dev")
  public IntegreradeEnheterBootstrapBean integreradeEnheterBootstrapBean() {
    return new IntegreradeEnheterBootstrapBean();
  }

  @Bean
  @Profile("dev")
  public UtkastBootstrapBean utkastBootstrapBean() {
    return new UtkastBootstrapBean();
  }
}