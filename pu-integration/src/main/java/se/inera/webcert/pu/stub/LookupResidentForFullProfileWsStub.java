package se.inera.webcert.pu.stub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import se.inera.population.residentmaster.v1.LookupResidentForFullProfileResponderInterface;
import se.inera.population.residentmaster.v1.PersonpostTYPE;
import se.inera.population.residentmaster.v1.ResidentType;
import se.inera.population.residentmaster.v1.lookupresidentforfullprofile.LookupResidentForFullProfileResponseType;
import se.inera.population.residentmaster.v1.lookupresidentforfullprofile.LookupResidentForFullProfileType;

import java.util.ArrayList;
import java.util.List;

public class LookupResidentForFullProfileWsStub implements LookupResidentForFullProfileResponderInterface {

    @Autowired
    private PersonStore personer;

    @Override
    public LookupResidentForFullProfileResponseType lookupResidentForFullProfile(String logicalAddress, LookupResidentForFullProfileType parameters) {
        validate(logicalAddress, parameters);
        LookupResidentForFullProfileResponseType response = new LookupResidentForFullProfileResponseType();
        for (String id : parameters.getPersonId()) {
            PersonpostTYPE personPost = personer.get(id);

            if (personPost != null) {
                ResidentType resident = new ResidentType();
                resident.setPersonpost(personPost);
                response.getResident().add(resident);
            }
        }
        return response;
    }

    private void validate(String logicalAddress, LookupResidentForFullProfileType parameters) {
        List<String> messages = new ArrayList<>();
        if (logicalAddress == null || logicalAddress.length() == 0) {
            messages.add("logicalAddress can not be null or empty");
        }
        if (parameters == null) {
            messages.add("LookupResidentForFullProfileType can not be null");
        } else {
            if (parameters.getPersonId().isEmpty()) {
                messages.add("At least one personId must be supplied");
            }
            if (parameters.getLookUpSpecification() == null) {
                messages.add("LookupSpecification must be included");
            }
        }

        if (messages.size() > 0) {
            throw new IllegalArgumentException(StringUtils.collectionToCommaDelimitedString(messages));
        }
    }
}
