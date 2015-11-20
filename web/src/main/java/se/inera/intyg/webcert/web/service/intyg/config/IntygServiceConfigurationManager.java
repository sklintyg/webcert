package se.inera.intyg.webcert.web.service.intyg.config;

public interface IntygServiceConfigurationManager {

    <T> T unmarshallConfig(String configAsJson, Class<T> configClazz);

    String marshallConfig(Object config);
}
