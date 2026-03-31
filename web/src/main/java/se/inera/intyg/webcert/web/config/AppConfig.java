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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import se.inera.intyg.common.support.modules.converter.SummaryConverter;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.common.support.services.BefattningService;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.infra.security.common.cookie.IneraCookieSerializer;
import se.inera.intyg.webcert.integration.analytics.config.CertificateAnalyticsServiceIntegrationConfig;
import se.inera.intyg.webcert.integration.fmb.config.FmbServicesConfig;
import se.inera.intyg.webcert.integration.privatepractitioner.config.PrivatePractitionerRestClientConfig;
import se.inera.intyg.webcert.integration.servicenow.config.ServiceNowIntegrationConfig;
import se.inera.intyg.webcert.integration.servicenow.stub.config.ServiceNowStubConfig;
import se.inera.intyg.webcert.mailstub.config.MailStubConfig;
import se.inera.intyg.webcert.persistence.config.JpaConfigBase;
import se.inera.intyg.webcert.web.bootstrap.UtkastBootstrapBean;
import se.inera.intyg.webcert.web.service.util.FragaSvarBootstrapBean;
import se.inera.intyg.webcert.web.service.util.IntegreradeEnheterBootstrapBean;

@Configuration
@DependsOn("dbUpdate")
@RequiredArgsConstructor
@EnableTransactionManagement
@PropertySources({
  @PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true),
  @PropertySource(value = "classpath:version.properties", ignoreResourceNotFound = true),
  @PropertySource(
      value = "classpath:webcert-notification-route-params.properties",
      ignoreResourceNotFound = true),
  @PropertySource(value = "file:${dev.config.file:-}", ignoreResourceNotFound = true),
})
@ComponentScans({
  @ComponentScan("se.inera.intyg.webcert.web"),
  @ComponentScan("se.inera.intyg.webcert.common"),
  @ComponentScan("se.inera.intyg.webcert.mailstub"),
  @ComponentScan("se.inera.intyg.webcert.persistence"),
  @ComponentScan("se.inera.intyg.webcert.integration.servicenow.config"),
  @ComponentScan("se.inera.intyg.webcert.integration.privatepractitioner.config"),
  @ComponentScan("se.inera.intyg.webcert.integration.analytics.config"),
  @ComponentScan("se.inera.intyg.webcert.integration.fmb.services"),
  @ComponentScan("se.inera.intyg.webcert.infra.xmldsig.config"),
  @ComponentScan("se.inera.intyg.webcert.infra.dynamiclink"),
  @ComponentScan("se.inera.intyg.webcert.infra.postnummer"),
  @ComponentScan("se.inera.intyg.webcert.infra.sjukfall.services"),
  @ComponentScan("se.inera.intyg.webcert.infra.integration.intygproxyservice"),
  @ComponentScan("se.inera.intyg.webcert.infra.pu.integration.intygproxyservice"),
  @ComponentScan("se.inera.intyg.webcert.infra.ia.config"),
  @ComponentScan("se.inera.intyg.webcert.infra.ia.cache"),
  @ComponentScan("se.inera.intyg.webcert.infra.srs"),
  @ComponentScan("se.inera.intyg.webcert.infra.security.filter"),
  @ComponentScan("se.inera.intyg.common"),
})
@Import({
  LoggingConfig.class,
  JmsConfig.class,
  JpaConfigBase.class,
  CacheConfig.class,
  JobConfig.class,
  MailConfig.class,
  MailStubConfig.class,
  CxfWsClientConfig.class,
  FmbServicesConfig.class,
  ServiceNowIntegrationConfig.class,
  ServiceNowStubConfig.class,
  CertificateAnalyticsServiceIntegrationConfig.class,
  PrivatePractitionerRestClientConfig.class,
  AuthoritiesConfig.class,
})
@ImportResource({
  "classpath:notification-sender-config.xml",
})
public class AppConfig implements TransactionManagementConfigurer {

  private final JpaTransactionManager transactionManager;

  @Value("${webcert.cookie.domain.name:}")
  private String webcertCookieDomainName;

  @Override
  public PlatformTransactionManager annotationDrivenTransactionManager() {
    return transactionManager;
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
    PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
    configurer.setIgnoreUnresolvablePlaceholders(true);
    configurer.setIgnoreResourceNotFound(true);
    return configurer;
  }

  @Bean
  public CookieSerializer cookieSerializer() {
    /*
    This is needed to make IdP functionality work.
    This will not satisfy all browsers, but it works for IE, Chrome and Edge.
    Reference: https://auth0.com/blog/browser-behavior-changes-what-developers-need-to-know/
     */
    final var ineraCookieSerializer = new IneraCookieSerializer();
    if (!webcertCookieDomainName.isBlank()) {
      ineraCookieSerializer.setDomainName(webcertCookieDomainName);
    }
    return ineraCookieSerializer;
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
  public ObjectMapper objectMapper() {
    return new CustomObjectMapper();
  }

  @Bean
  @Profile({"dev", "wc-init-data"})
  public FragaSvarBootstrapBean fragaSvarBootstrapBean() {
    return new FragaSvarBootstrapBean();
  }

  @Bean
  @Profile({"dev", "wc-init-data"})
  public IntegreradeEnheterBootstrapBean integreradeEnheterBootstrapBean() {
    return new IntegreradeEnheterBootstrapBean();
  }

  @Bean
  @Profile({"dev", "wc-init-data", "test", "demo"})
  public UtkastBootstrapBean utkastBootstrapBean() {
    return new UtkastBootstrapBean();
  }
}
