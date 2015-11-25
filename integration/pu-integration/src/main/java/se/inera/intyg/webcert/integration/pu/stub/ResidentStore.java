package se.inera.intyg.webcert.integration.pu.stub;

import java.util.HashMap;
import java.util.Map;

import se.inera.population.residentmaster.v1.ResidentType;

public class ResidentStore {

    private final Map<String, ResidentType> residents = new HashMap<>();

    public void addUser(ResidentType residentPost) {
        residents.put(residentPost.getPersonpost().getPersonId(), residentPost);
    }

    public ResidentType get(String id) {
        return residents.get(id);
    }
}
