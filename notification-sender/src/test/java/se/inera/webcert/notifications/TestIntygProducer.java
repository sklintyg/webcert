package se.inera.webcert.notifications;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.webcert.persistence.intyg.model.Intyg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.joda.ser.LocalDateTimeSerializer;

public class TestIntygProducer {
    
    private static final Logger LOG = LoggerFactory.getLogger(TestIntygProducer.class);

    private ObjectMapper objMapper;
           
    public Intyg buildIntyg(String pathToJsonFile) {
        try {
            String intygJson = TestDataUtil.readRequestFromFile(pathToJsonFile);
            if (intygJson == null) {
                throw new RuntimeException("No intyg json found on: " + pathToJsonFile);
            }
            ObjectMapper objectMapper = getObjectMapper();
            return objectMapper.readValue(intygJson, Intyg.class);
        } catch (Exception e) {
            throw new RuntimeException("Error occured when reading intyg json: " + e.getMessage());
        }
    }
        
    private ObjectMapper getObjectMapper() {
        if (this.objMapper == null) {
            this.objMapper = initObjectMapper();
        }
        return this.objMapper;
    }
    
    private ObjectMapper initObjectMapper() {
        
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        
        objMapper.registerModule(module);
        
        return objMapper;
    }
}
