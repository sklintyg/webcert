package se.inera.webcert.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.inera.webcert.security.Vardenhet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author johannesc
 */
public class Vardenheter {

    private List<Vardenhet> vardenheter = new ArrayList<>();

    public List<Vardenhet> getVardenheter() {
        return vardenheter;
    }

    public String stringify() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
