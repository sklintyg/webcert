package se.inera.intyg.webcert.integration.pp.stub;

import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Magnus Ekstrand on 18/06/15.
 */
public class HoSPersonStub {

    private Map<String, HoSPersonType> personer = new HashMap<>();

    public void add(HoSPersonType person) {
        personer.put(person.getPersonId().getExtension(), person);
    }

    public HoSPersonType get(String id) {
        return personer.get(id);
    }

    public HoSPersonType getByHsaId(String hsaId) {
        if (hsaId != null) {
            for (HoSPersonType person : personer.values()) {
                if (hsaId.equalsIgnoreCase(person.getHsaId().getExtension())) {
                    return person;
                }
            }
        }
        return null;
    }

}
