package se.inera.webcert.pu.stub;

import se.inera.population.residentmaster.v1.PersonpostTYPE;

import java.util.HashMap;
import java.util.Map;

public class PersonStore {
    private final Map<String, PersonpostTYPE> personer = new HashMap<>();

    public void addUser(PersonpostTYPE personPost) {
        personer.put(personPost.getPersonId(), personPost);
    }


    public PersonpostTYPE get(String id) {
        return personer.get(id);
    }
}
