/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.integration.pp.services;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.inera.intyg.webcert.integration.pp.stub.GetPrivatePractitionerResponderStub;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;

import javax.xml.ws.WebServiceException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:PPServiceTest/test-context.xml")
public class PPServiceTest {

    @Autowired
    private PPService service;


    @Test
    public void checkExistingPerson() {
        HoSPersonType hoSPersonType = service.getPrivatePractitioner("address", null, GetPrivatePractitionerResponderStub.PERSONNUMMER_EXISTING);
        assertNotNull(hoSPersonType);
        assertEquals(hoSPersonType.getPersonId().getExtension(), GetPrivatePractitionerResponderStub.PERSONNUMMER_EXISTING);
    }

    @Test
    public void checkNonExistingPerson() {
        HoSPersonType hoSPersonType = service.getPrivatePractitioner("address", null, GetPrivatePractitionerResponderStub.PERSONNUMMER_NONEXISTING);
        assertNull(hoSPersonType);
    }

    @Test
    public void whenServiceResultCodeIsErrorThenExpectNullResponse() {
        HoSPersonType hoSPersonType = service.getPrivatePractitioner("address", null, GetPrivatePractitionerResponderStub.PERSONNUMMER_ERROR_RESPONSE);
        assertNull(hoSPersonType);
    }

    @Test(expected = WebServiceException.class)
    public void whenUnexpectedErrorThenExpectException() {
        service.getPrivatePractitioner("address", null, GetPrivatePractitionerResponderStub.PERSONNUMMER_THROW_EXCEPTION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenHsaIdAndPersonalIdIsNullThenExceptionThrown() {
        service.getPrivatePractitioner("address", null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenHsaIdAndPersonalIdIsSetThenExceptionThrown() {
        service.getPrivatePractitioner("address", "any HSA-ID", "any PERSONNUMMER");
    }

}
