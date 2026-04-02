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
package se.inera.intyg.webcert.notification_sender.certificatesender.config;

import org.apache.camel.impl.engine.ExplicitCamelContextNameStrategy;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.intyg.webcert.notification_sender.certificatesender.routes.CertificateRouteBuilder;
import se.inera.intyg.webcert.notification_sender.certificatesender.services.CertificateRevokeProcessor;
import se.inera.intyg.webcert.notification_sender.certificatesender.services.CertificateSendProcessor;
import se.inera.intyg.webcert.notification_sender.certificatesender.services.CertificateStoreProcessor;
import se.inera.intyg.webcert.notification_sender.certificatesender.services.RegisterApprovedReceiversProcessor;
import se.inera.intyg.webcert.notification_sender.certificatesender.services.SendMessageToRecipientProcessor;

/**
 * Replaces certificates/beans-context.xml and certificates/camel-context.xml. Defines all
 * certificate processor beans and creates the webcertCertificateSender CamelContext.
 */
@Configuration
public class CertificateCamelConfig {

  @Bean
  public CertificateStoreProcessor certificateStoreProcessor() {
    return new CertificateStoreProcessor();
  }

  @Bean
  public CertificateSendProcessor certificateSendProcessor() {
    return new CertificateSendProcessor();
  }

  @Bean
  public CertificateRevokeProcessor certificateRevokeProcessor() {
    return new CertificateRevokeProcessor();
  }

  @Bean
  public SendMessageToRecipientProcessor sendMessageToRecipientProcessor() {
    return new SendMessageToRecipientProcessor();
  }

  @Bean
  public RegisterApprovedReceiversProcessor registerApprovedReceiversProcessor() {
    return new RegisterApprovedReceiversProcessor();
  }

  @Bean
  public CertificateRouteBuilder certificateRouteBuilder() {
    return new CertificateRouteBuilder();
  }

  @Bean
  public SpringCamelContext webcertCertificateSender(
      ApplicationContext applicationContext, CertificateRouteBuilder certificateRouteBuilder) {
    SpringCamelContext context = new SpringCamelContext(applicationContext);
    context.setNameStrategy(new ExplicitCamelContextNameStrategy("webcertCertificateSender"));
    try {
      context.addRoutes(certificateRouteBuilder);
    } catch (Exception e) {
      throw new BeanCreationException("webcertCertificateSender", "Failed to add routes", e);
    }
    return context;
  }
}
