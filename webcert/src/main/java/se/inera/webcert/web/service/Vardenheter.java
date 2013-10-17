package se.inera.webcert.web.service;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.inera.webcert.hsa.model.Vardenhet;

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
