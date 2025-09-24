package se.inera.intyg.webcert.integration.analytics.config;

import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@ComponentScan(basePackages = {
    "se.inera.intyg.webcert.integration.analytics"
})
public class CertificateAnalyticsServiceIntegrationConfig {

    @Value("${certificate.analytics.event.queueName}")
    private String queueName;

    @Bean
    public MappingJackson2MessageConverter messageConverter() {
        final var converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Bean
    public JmsTemplate jmsTemplateForCertificateEvent(ConnectionFactory connectionFactory, MappingJackson2MessageConverter converter) {
        final var jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setDefaultDestinationName(queueName);
        jmsTemplate.setMessageConverter(converter);
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }
}
