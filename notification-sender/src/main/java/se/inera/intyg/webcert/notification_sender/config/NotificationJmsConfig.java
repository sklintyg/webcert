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
package se.inera.intyg.webcert.notification_sender.config;

import jakarta.jms.ConnectionFactory;
import org.apache.camel.LoggingLevel;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.support.destination.DestinationResolver;

/**
 * Replaces jms-context.xml. Defines the Camel JMS bridge: the ActiveMQ Camel component, its
 * JmsConfiguration, and the transactional policy used by Camel routes.
 *
 * <p>Depends on jmsConnectionFactory, jmsTransactionManager, and jmsDestinationResolver from
 * JmsConfig.java (web module), shared via the same Spring context.
 */
@Configuration
public class NotificationJmsConfig {

  @Bean
  public JmsConfiguration camelJmsConfiguration(
      ConnectionFactory jmsConnectionFactory, DestinationResolver jmsDestinationResolver) {
    JmsConfiguration config = new JmsConfiguration();
    config.setErrorHandlerLoggingLevel(LoggingLevel.OFF);
    config.setErrorHandlerLogStackTrace(false);
    config.setConnectionFactory(jmsConnectionFactory);
    config.setDestinationResolver(jmsDestinationResolver);
    return config;
  }

  // Bean name "jms" is the Camel component name — routes use "jms:queue:..." URIs.
  @Bean
  public ActiveMQComponent jms(JmsConfiguration camelJmsConfiguration) {
    ActiveMQComponent component = new ActiveMQComponent();
    component.setConfiguration(camelJmsConfiguration);
    component.setTransacted(true);
    component.setCacheLevelName("CACHE_CONSUMER");
    return component;
  }

  // Bean name "txTemplate" is fixed — routes call .transacted("txTemplate")
  @Bean
  public SpringTransactionPolicy txTemplate(JmsTransactionManager jmsTransactionManager) {
    return new SpringTransactionPolicy(jmsTransactionManager);
  }
}
