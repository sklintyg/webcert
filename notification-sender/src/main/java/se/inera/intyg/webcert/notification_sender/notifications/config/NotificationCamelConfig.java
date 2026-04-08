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
package se.inera.intyg.webcert.notification_sender.notifications.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.impl.engine.ExplicitCamelContextNameStrategy;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteBuilder;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationAggregator;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTransformer;

/**
 * Replaces notifications/beans-context.xml and notifications/camel-context.xml.
 *
 * <p>Intentionally does NOT include notificationPatientEnricher — that bean is declared in
 * NotificationSenderConfig so unit/integration tests can load this config without pulling in
 * PU-service (Ignite) dependencies.
 */
@Configuration
public class NotificationCamelConfig {

  @Bean
  public NotificationTransformer notificationTransformer() {
    return new NotificationTransformer();
  }

  @Bean
  public NotificationAggregator notificationAggregator() {
    return new NotificationAggregator();
  }

  @Bean
  public NotificationRouteBuilder processNotificationRequestRouteBuilder() {
    return new NotificationRouteBuilder();
  }

  @Bean
  public JacksonDataFormat notificationMessageDataFormat(ObjectMapper objectMapper) {
    return new JacksonDataFormat(objectMapper, NotificationMessage.class);
  }

  @Bean
  public SpringCamelContext webcertNotification(
      ApplicationContext applicationContext,
      NotificationRouteBuilder processNotificationRequestRouteBuilder) {
    SpringCamelContext context = new SpringCamelContext(applicationContext);
    context.setNameStrategy(new ExplicitCamelContextNameStrategy("webcertNotification"));
    try {
      context.addRoutes(processNotificationRequestRouteBuilder);
    } catch (Exception e) {
      throw new BeanCreationException("webcertNotification", "Failed to add routes", e);
    }
    return context;
  }
}