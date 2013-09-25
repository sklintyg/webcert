package se.inera.webcert.hsa.stub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author johannesc
 */
public class HsaServiceStub {

    // Data cache

    // Map of vardgivarid and a collection of units. Inner map is unit's hsa id plus unit
    private Map<String, Map<String, HsaUnitStub>> vardgivare = new HashMap();
    private Map<String, PersonStub> persons = new HashMap();


    public HsaUnitStub getHsaUnit(String hsaIdentity) {
        for (Map<String, HsaUnitStub> enheter : vardgivare.values()) {
            for (HsaUnitStub unit : enheter.values()) {
                if (unit.getHsaId().equals(hsaIdentity)) {
                    return unit;
                }
            }
        }
        return null;
    }

    public void addHsaUnit(HsaUnitStub unit) {
        Map<String, HsaUnitStub> enheter = this.vardgivare.get(unit.getVardgivarid());
        if (enheter == null) {
            this.vardgivare.put(unit.getVardgivarid(), new HashMap());
        }
        this.vardgivare.get(unit.getVardgivarid()).put(unit.getHsaId(), unit);
    }

    public void deleteHsaUnit(String id) {
        HsaUnitStub unitToDelete = getHsaUnit(id);
        vardgivare.get(unitToDelete.getVardgivarid()).remove(unitToDelete.getHsaId());
    }

    public void addPerson(PersonStub person) {
        HsaUnitStub enhet;
        for (String unitId : person.getMedarbetaruppdrag()) {
            enhet = getHsaUnit(unitId);
            if (enhet == null) {
                throw new RuntimeException(("HSA unit does not exist: " + unitId));
            }
            enhet.getMedarbetaruppdrag().add(person);
        }
        persons.put(person.getHsaId(), person);
    }

    public void deletePerson(String id) {
        for (Map<String, HsaUnitStub> enheter : vardgivare.values()) {
            for (HsaUnitStub unit : enheter.values()) {
                PersonStub person = getPersonFromList(id, unit.getMedarbetaruppdrag());
                if (person != null) {
                    unit.getMedarbetaruppdrag().remove(person);
                }
            }
        }
        persons.remove(id);
    }

    private PersonStub getPersonFromList(String hsaId, List<PersonStub> personer) {
        for (PersonStub person : personer) {
            if (person.getHsaId().equals(hsaId)) {
                return person;
            }
        }
        return null;
    }

    public PersonStub getPerson(String hsaIdentity) {
        return persons.get(hsaIdentity);
    }
}
