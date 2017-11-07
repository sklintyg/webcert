package se.inera.intyg.webcert.integration.tak.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConsumerConfig {

    @Value("${tak.connection.request.timeout}")
    private int requestTimeout;

    @Value("${tak.connection.timeout}")
    private int connectionTimeout;

    @Value("${tak.read.timeout}")
    private int readTimeout;

    @Bean
    public RestTemplate customRestTemplate() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(requestTimeout);
        httpRequestFactory.setConnectTimeout(connectionTimeout);
        httpRequestFactory.setReadTimeout(readTimeout);

        return new RestTemplate(httpRequestFactory);
    }
}
