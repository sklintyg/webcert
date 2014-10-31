package se.inera.webcert.service.intyg.config;

public interface IntygServiceConfigurationManager {

    public abstract <T> T unmarshallConfig(String configAsJson, Class<T> configClazz);
    
    public abstract String marshallConfig(Object config);

}
