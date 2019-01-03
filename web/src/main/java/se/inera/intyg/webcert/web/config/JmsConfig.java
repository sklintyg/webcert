/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

/**
 * Configures JMS.
 */
@Configuration
@EnableJms
public class JmsConfig {

    @Value("${activemq.broker.url}")
    private String activeMqBrokerUrl;

    @Value("${activemq.broker.username}")
    private String activeMqBrokerUsername;

    @Value("${activemq.broker.password}")
    private String activeMqBrokerPassword;

    @Value("${log.queueName}")
    private String logQueueName;

    @Value("${certificate.sender.queueName}")
    private String certificateSenderQueueName;

    @Value("${notification.ws.queueName}")
    private String notificationWSQueueName;

    @Value("${notification.aggregation.queueName}")
    private String notificationAggregationQueueName;

    @Bean
    public JmsListenerContainerFactory jmsListenerContainerFactory(JmsTransactionManager jmsTransactionManager) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(jmsTransactionManager.getConnectionFactory());
        factory.setDestinationResolver(jmsDestinationResolver());
        factory.setSessionTransacted(true);
        factory.setTransactionManager(jmsTransactionManager);
        factory.setCacheLevelName("CACHE_CONSUMER");
        factory.setConcurrency("1-10");
        return factory;
    }

    @Bean
    public DestinationResolver jmsDestinationResolver() {
        return new DynamicDestinationResolver();
    }

    @Bean
    public JmsTransactionManager jmsTransactionManager(ConnectionFactory jmsConnectionFactory) {
        return new JmsTransactionManager(jmsConnectionFactory);
    }

    @Bean
    public ConnectionFactory jmsConnectionFactory() {
        return new CachingConnectionFactory(
                new ActiveMQConnectionFactory(activeMqBrokerUsername, activeMqBrokerPassword, activeMqBrokerUrl)
        );
    }

    @Bean
    @Profile({ "dev", "testability-api" })
    public JmsTemplate jmsPDLLogTemplateNoTx(ConnectionFactory jmsConnectionFactory) {
        final JmsTemplate t = jmsPDLLogTemplate(jmsConnectionFactory);
        t.setSessionTransacted(false);
        return t;
    }

    @Bean
    public JmsTemplate jmsPDLLogTemplate(ConnectionFactory jmsConnectionFactory) {
        return template(jmsConnectionFactory, logQueueName);
    }

    @Bean
    public JmsTemplate jmsNotificationTemplateForAggregation(ConnectionFactory jmsConnectionFactory) {
        return template(jmsConnectionFactory, notificationAggregationQueueName);
    }

    @Bean
    public JmsTemplate jmsCertificateSenderTemplate(ConnectionFactory jmsConnectionFactory) {
        return template(jmsConnectionFactory, certificateSenderQueueName);
    }


    JmsTemplate template(final ConnectionFactory connectionFactory, final String queueName) {
        final JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestinationName(queueName);
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }

}
