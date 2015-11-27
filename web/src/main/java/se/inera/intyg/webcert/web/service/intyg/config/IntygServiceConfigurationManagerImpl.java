package se.inera.intyg.webcert.web.service.intyg.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class IntygServiceConfigurationManagerImpl implements IntygServiceConfigurationManager {

    private static final Logger LOG = LoggerFactory.getLogger(IntygServiceConfigurationManagerImpl.class);

    @Autowired
    private ObjectMapper objectMapper;

    public IntygServiceConfigurationManagerImpl() {

    }

    public IntygServiceConfigurationManagerImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T unmarshallConfig(String configAsJson, Class<T> configClazz) {
        try {
            return objectMapper.readValue(configAsJson, configClazz);
        } catch (IOException e) {
            LOG.error("Module problems occured when trying to unmarshall configation.", e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
    }

    @Override
    public String marshallConfig(Object config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            LOG.error("Module problems occured when trying to create and marshall configuration.", e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
