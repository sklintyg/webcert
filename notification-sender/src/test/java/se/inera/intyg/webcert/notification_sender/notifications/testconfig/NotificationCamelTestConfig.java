/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notification_sender.notifications.testconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationPostProcessor;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTransformer;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.NotificationWSSender;

@ImportResource(locations = "classpath:notifications/unit-test-notification-sender-config.xml")
public class NotificationCamelTestConfig {

    // Presence of below bean definitions is required during setup of routes for the imported camel context.
    // Since the beans are mocked during testing the return values have intentionally been set to null to
    // avoid having to import further dependencies.

    @Bean
    public NotificationTransformer notificationTransformer() {
        return null;
    }

    @Bean
    public NotificationWSSender notificationWSSender() {
        return null;
    }

    @Bean
    public NotificationPostProcessor notificationPostProcessor() {
        return null;
    }
}
