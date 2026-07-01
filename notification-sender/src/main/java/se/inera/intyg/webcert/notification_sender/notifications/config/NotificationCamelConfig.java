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

import java.io.InputStream;
import java.io.OutputStream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteBuilder;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationAggregator;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTransformer;
import tools.jackson.databind.json.JsonMapper;

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
  public DataFormat notificationMessageDataFormat() {
    JsonMapper mapper = new CustomObjectMapper();
    return new DataFormat() {
      @Override
      public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
        mapper.writeValue(stream, graph);
      }

      @Override
      public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
        return mapper.readValue(stream, NotificationMessage.class);
      }

      @Override
      public void start() {}

      @Override
      public void stop() {}
    };
  }
}
