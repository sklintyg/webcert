package se.inera.webcert.pu.stub;

import org.junit.Ignore;
import org.junit.Test;
import se.inera.population.residentmaster.v1.LookupResidentForFullProfileResponderInterface;
import se.inera.population.residentmaster.v1.lookupresidentforfullprofile.LookUpSpecificationType;
import se.inera.population.residentmaster.v1.lookupresidentforfullprofile.LookupResidentForFullProfileResponseType;
import se.inera.population.residentmaster.v1.lookupresidentforfullprofile.LookupResidentForFullProfileType;

import static org.junit.Assert.*;

public class LookupResidentForFullProfileWsStubTest {

    private LookupResidentForFullProfileResponderInterface ws = new LookupResidentForFullProfileWsStub();

    @Test (expected = IllegalArgumentException.class)
    public void nullAddressThrowsException() {
        ws.lookupResidentForFullProfile(null, defaultRequest());
        fail();
    }

    @Test (expected = IllegalArgumentException.class)
    public void emptyAddressThrowsException() {
        ws.lookupResidentForFullProfile("", defaultRequest());
        fail();
    }

    @Test (expected = IllegalArgumentException.class)
    public void nullParametersThrowsException() {
        ws.lookupResidentForFullProfile("address", null);
        fail();
    }

    @Test (expected = IllegalArgumentException.class)
    public void noPersonIdThrowsException() {
        LookupResidentForFullProfileType parameters = defaultRequest();
        parameters.getPersonId().clear();
        LookupResidentForFullProfileResponseType result = ws.lookupResidentForFullProfile("address", parameters);
        fail();
    }

    @Test
    @Ignore
    public void wrongPersonIdFormatReturnsError() {
        LookupResidentForFullProfileType parameters = defaultRequest();
        parameters.getPersonId().clear();
        parameters.getPersonId().add("1212121212");
        LookupResidentForFullProfileResponseType address = ws.lookupResidentForFullProfile("address", parameters);
        fail("How do we generate an ResultCode error?");
    }

    @Test (expected = IllegalArgumentException.class)
    public void noLookupSpecificationThrowsException() {
        LookupResidentForFullProfileType parameters = defaultRequest();
        parameters.setLookUpSpecification(null);
        LookupResidentForFullProfileResponseType address = ws.lookupResidentForFullProfile("address", parameters);
        fail();
    }

    @Test
    public void personIdParametersReturned() {
        LookupResidentForFullProfileType parameters = defaultRequest();
        LookupResidentForFullProfileResponseType address = ws.lookupResidentForFullProfile("address", parameters);
        assertEquals(1, address.getResident().size());
    }

    private LookupResidentForFullProfileType defaultRequest() {
        LookupResidentForFullProfileType parameters = new LookupResidentForFullProfileType();
        parameters.getPersonId().add("191212121212");
        LookUpSpecificationType specification = new LookUpSpecificationType();
        parameters.setLookUpSpecification(specification);
        return parameters;
    }

}
