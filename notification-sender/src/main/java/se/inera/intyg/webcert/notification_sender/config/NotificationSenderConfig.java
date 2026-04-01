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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import se.inera.intyg.webcert.notification_sender.certificatesender.config.CertificateCamelConfig;
import se.inera.intyg.webcert.notification_sender.notifications.config.NotificationCamelConfig;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationPatientEnricher;

/**
 * Root configuration for the notification-sender module. Replaces notification-sender-config.xml.
 *
 * notificationPatientEnricher is declared here (not in NotificationCamelConfig) so that
 * unit/integration tests can load NotificationCamelConfig directly without pulling in
 * PU-service (Ignite) transitive dependencies.
 */
@Configuration
@ComponentScan("se.inera.intyg.webcert.notification_sender.notifications")
@Import({
    NotificationJmsConfig.class,
    NotificationWsClientConfig.class,
    NotificationCamelConfig.class,
    CertificateCamelConfig.class,
})
public class NotificationSenderConfig {

    @Bean
    public NotificationPatientEnricher notificationPatientEnricher() {
        return new NotificationPatientEnricher();
    }
}
