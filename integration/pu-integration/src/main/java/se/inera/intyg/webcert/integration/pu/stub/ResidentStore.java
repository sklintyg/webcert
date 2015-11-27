package se.inera.intyg.webcert.integration.pu.stub;

import se.riv.population.residentmaster.types.v1.ResidentType;

import java.util.HashMap;
import java.util.Map;

public class ResidentStore {

    private final Map<String, ResidentType> residents = new HashMap<>();

    public void addUser(ResidentType residentPost) {
        residents.put(residentPost.getPersonpost().getPersonId(), residentPost);
    }

    public ResidentType get(String id) {
        return residents.get(id);
    }
}
