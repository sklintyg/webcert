package se.inera.intyg.webcert.integration.pu.stub;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import se.riv.population.residentmaster.lookupresidentforfullprofileresponder.v1.LookupResidentForFullProfileResponseType;
import se.riv.population.residentmaster.lookupresidentforfullprofileresponder.v1.LookupResidentForFullProfileType;
import se.riv.population.residentmaster.lookupresidentforfullprofileresponder.v11.LookupResidentForFullProfileResponderInterface;
import se.riv.population.residentmaster.types.v1.ResidentType;


public class LookupResidentForFullProfileWsStub implements LookupResidentForFullProfileResponderInterface {

    @Autowired
    private ResidentStore personer;

    @Override
    public LookupResidentForFullProfileResponseType lookupResidentForFullProfile(String logicalAddress, LookupResidentForFullProfileType parameters) {
        validate(logicalAddress, parameters);
        LookupResidentForFullProfileResponseType response = new LookupResidentForFullProfileResponseType();
        for (String id : parameters.getPersonId()) {
            ResidentType residentPost = personer.get(id);

            if (residentPost != null) {
                response.getResident().add(residentPost);
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
