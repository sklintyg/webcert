package se.inera.intyg.webcert.integration.pp.stub;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.webcert.integration.pp.util.ObjectCreator;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.ResultCodeEnum;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitioner.v1.rivtabp21.ValidatePrivatePractitionerResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitionerresponder.v1.ValidatePrivatePractitionerResponseType;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitionerresponder.v1.ValidatePrivatePractitionerType;

@RunWith(MockitoJUnitRunner.class)
public class ValidatePrivatePractitionerResponderTest {

    private final static String PERSONNUMMER = "19121212-1212";

    @Mock
    private HoSPersonStub personStub;

    @InjectMocks
    private ValidatePrivatePractitionerResponderInterface ws = new ValidatePrivatePractitionerResponderStub();

    @Test (expected = IllegalArgumentException.class)
    public void nullParametersThrowsException() {
        ws.validatePrivatePractitioner(null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void noPersonIdThrowsException() {
        ValidatePrivatePractitionerType request = new ValidatePrivatePractitionerType();
        ws.validatePrivatePractitioner(null, request);
    }

    @Test
    public void verifyExistingPerson() {

        // Given
        ObjectCreator objectCreator = new ObjectCreator();
        HoSPersonType hoSPersonType = objectCreator.getHoSPersonType();

        ValidatePrivatePractitionerType request = defaultRequest();

        ValidatePrivatePractitionerResponseType expected = new ValidatePrivatePractitionerResponseType();
        expected.setResultCode(ResultCodeEnum.OK);

        // When
        when(personStub.get(PERSONNUMMER)).thenReturn(hoSPersonType);

        // Call web service
        ValidatePrivatePractitionerResponseType actual = ws.validatePrivatePractitioner("address", request);

        // Then
        assertTrue(ResultCodeEnum.OK == actual.getResultCode());

        verify(personStub, times(1)).get(PERSONNUMMER);
    }

    @Test
    public void verifyNonExistingPerson() {

        // Given
        ValidatePrivatePractitionerType request = defaultRequest();
        request.setPersonalIdentityNumber("1901010101-0101");

        // When
        when(personStub.get("1901010101-0101")).thenReturn(null);

        // Call web service
        ValidatePrivatePractitionerResponseType actual = ws.validatePrivatePractitioner("address", request);

        // Then
        assertTrue(ResultCodeEnum.ERROR == actual.getResultCode());

        verify(personStub, times(1)).get("1901010101-0101");
    }

    private ValidatePrivatePractitionerType defaultRequest() {

        ValidatePrivatePractitionerType request = new ValidatePrivatePractitionerType();
        request.setPersonalIdentityNumber(PERSONNUMMER);
        return request;
    }


}
