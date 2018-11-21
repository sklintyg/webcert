/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.integration.pp.stub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.webcert.integration.pp.util.ObjectCreator;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitioner.v1.rivtabp21.GetPrivatePractitionerResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerResponseType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.ResultCodeEnum;

@RunWith(MockitoJUnitRunner.class)
public class GetPrivatePractitionerResponderTest {

    private final static String HSAID        = "HSA0000-123456789";
    private final static String PERSONNUMMER = "19121212-1212";

    @Mock
    private HoSPersonStub personStub;

    @InjectMocks
    private GetPrivatePractitionerResponderInterface ws = new GetPrivatePractitionerResponderStub();

    @Test (expected = IllegalArgumentException.class)
    public void nullParametersThrowsException() {
        ws.getPrivatePractitioner(null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void noPersonIdThrowsException() {
        GetPrivatePractitionerType request = new GetPrivatePractitionerType();
        ws.getPrivatePractitioner(null, request);
    }

    @Test
    public void verifyExistingPerson() {

        // Given
        ObjectCreator objectCreator = new ObjectCreator();
        HoSPersonType hoSPersonType = objectCreator.getHoSPersonType();

        GetPrivatePractitionerType request = defaultRequest();

        GetPrivatePractitionerResponseType expected = new GetPrivatePractitionerResponseType();
        expected.setResultCode(ResultCodeEnum.OK);
        expected.setHoSPerson(hoSPersonType);

        // When
        when(personStub.get(PERSONNUMMER)).thenReturn(hoSPersonType);

        // Call web service
        GetPrivatePractitionerResponseType actual = ws.getPrivatePractitioner("address", request);

        // Then
        assertTrue(ResultCodeEnum.OK == actual.getResultCode());
        assertEquals(HSAID, actual.getHoSPerson().getHsaId().getExtension());
        assertEquals(PERSONNUMMER, actual.getHoSPerson().getPersonId().getExtension());

        verify(personStub, times(1)).get(PERSONNUMMER);
    }

    @Test
    public void verifyNonExistingPerson() {

        // Given
        GetPrivatePractitionerType request = defaultRequest();
        request.setPersonalIdentityNumber("1901010101-0101");

        // When
        when(personStub.get("1901010101-0101")).thenReturn(null);

        // Call web service
        GetPrivatePractitionerResponseType actual = ws.getPrivatePractitioner("address", request);

        // Then
        assertTrue(ResultCodeEnum.INFO == actual.getResultCode());
        assertNull(actual.getHoSPerson());

        verify(personStub, times(1)).get("1901010101-0101");
    }

    private GetPrivatePractitionerType defaultRequest() {

        GetPrivatePractitionerType request = new GetPrivatePractitionerType();
        request.setPersonalIdentityNumber(PERSONNUMMER);
        return request;
    }


}
