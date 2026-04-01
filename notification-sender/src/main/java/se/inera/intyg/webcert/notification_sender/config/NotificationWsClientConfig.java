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

import org.apache.cxf.Bus;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponderInterface;

/**
 * Replaces notifications/ws-context.xml. Configures the CXF bus with logging and defines
 * the outbound JAX-WS client for sending certificate status updates.
 */
@Configuration
public class NotificationWsClientConfig {

    @Autowired
    void configureBus(Bus bus) {
        bus.getFeatures().add(new LoggingFeature());
    }

    // Profile !dev matches the <beans profile="!dev"> in the original ws-context.xml
    @Bean
    @Profile("!dev")
    public CertificateStatusUpdateForCareResponderInterface certificateStatusUpdateForCareClientV3(
            @Value("${certificatestatusupdateforcare.ws.endpoint.v3.url}") String address) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(CertificateStatusUpdateForCareResponderInterface.class);
        factory.setAddress(address);
        factory.getProperties().put("schema-validation-enabled", true);
        return (CertificateStatusUpdateForCareResponderInterface) factory.create();
    }
}
