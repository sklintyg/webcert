package se.inera.intyg.webcert.integration.pp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by mango on 18/06/15.
 */
public class ObjectToJson {

    private ObjectMapper objectMapper = new ObjectMapper();

    private HoSPersonType hoSPersonType;

    public ObjectToJson(HoSPersonType hoSPersonType) {
        this.hoSPersonType = hoSPersonType;
    }

    public String printJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(hoSPersonType);
    }

}
