package se.inera.intyg.webcert.integration.pp.stub;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitioner.v1.rivtabp21.GetPrivatePractitionerResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerType;

@RunWith(MockitoJUnitRunner.class)
public class GetPrivatePractitionerResponderTest {

    @Mock
    private HoSPersonStub personStub;

    @InjectMocks
    private GetPrivatePractitionerResponderInterface ws = new GetPrivatePractitionerResponderStub();


    @Test
    public void personIdParametersReturned() {
/*
        PersonpostTYPE person = new PersonpostTYPE();
        person.setPersonId("191212121212");
        ResidentType resident = new ResidentType();
        resident.setPersonpost(person);

        when(personStub.get("191212121212")).thenReturn(resident);

        LookupResidentForFullProfileType parameters = defaultRequest();
        LookupResidentForFullProfileResponseType address = ws.lookupResidentForFullProfile("address", parameters);
        assertEquals(1, address.getResident().size());
        assertEquals("191212121212", address.getResident().get(0).getPersonpost().getPersonId());
*/
    }

    private GetPrivatePractitionerType defaultRequest() {
/*
        LookupResidentForFullProfileType parameters = new LookupResidentForFullProfileType();
        parameters.getPersonId().add("191212121212");
        LookUpSpecificationType specification = new LookUpSpecificationType();
        parameters.setLookUpSpecification(specification);
        return parameters;
*/
        return null;
    }


}
