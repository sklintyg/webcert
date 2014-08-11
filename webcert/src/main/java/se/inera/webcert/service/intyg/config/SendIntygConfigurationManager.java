package se.inera.webcert.service.intyg.config;

public interface SendIntygConfigurationManager {

    public abstract String createAndMarshallSendConfig(String recipient, boolean hasPatientConsent);

    public abstract SendIntygConfiguration unmarshallSendConfig(String configAsJson);
    
    public abstract String marshallSendConfig(SendIntygConfiguration sendConfig);

}
