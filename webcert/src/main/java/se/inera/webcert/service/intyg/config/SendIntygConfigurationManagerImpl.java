package se.inera.webcert.service.intyg.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;

@Component
public class SendIntygConfigurationManagerImpl implements SendIntygConfigurationManager {

    private static final Logger LOG = LoggerFactory.getLogger(SendIntygConfigurationManagerImpl.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public String createAndMarshallSendConfig(String recipient, boolean hasPatientConsent) {
        SendIntygConfiguration sendConfig = new SendIntygConfiguration(recipient, hasPatientConsent);
        return marshallSendConfig(sendConfig);
    }
    
    public String marshallSendConfig(SendIntygConfiguration sendConfig) {
        try {
            return objectMapper.writeValueAsString(sendConfig);
        } catch (JsonProcessingException e) {
            LOG.error("Module problems occured when trying to create and marshall send intyg configuration.", e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
    }
    
    @Override
    public SendIntygConfiguration unmarshallSendConfig(String configAsJson) {
        try {
            return objectMapper.readValue(configAsJson, SendIntygConfiguration.class);
        } catch (IOException e) {
            LOG.error("Module problems occured when trying to unmarshall send intyg configation.", e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
        
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
}
