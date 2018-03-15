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
package se.inera.intyg.webcert.web.web.controller.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.api.dto.PersonuppgifterResponse;

import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PersonApiControllerTest {

    @Mock
    private PUService puService;

    @Mock
    private MonitoringLogService mockMonitoringService;

    @InjectMocks
    private PersonApiController personCtrl = new PersonApiController();

    @Test
    public void testGetPersonuppgifter() {
        Personnummer personnummer = createPnr("19121212-1212");

        when(puService.getPerson(any(Personnummer.class))).thenReturn(new PersonSvar(
                new Person(personnummer, false, false, "fnamn", "mnamn", "enamn", "paddr", "pnr", "port"), PersonSvar.Status.FOUND));

        Response response = personCtrl.getPersonuppgifter(personnummer.getPersonnummer());

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        PersonuppgifterResponse res = (PersonuppgifterResponse) response.getEntity();
        assertEquals(PersonSvar.Status.FOUND, res.getStatus());
        assertEquals(false, res.getPerson().isSekretessmarkering());
        assertEquals("fnamn", res.getPerson().getFornamn());
        assertEquals("mnamn", res.getPerson().getMellannamn());
        assertEquals("enamn", res.getPerson().getEfternamn());
        assertEquals("paddr", res.getPerson().getPostadress());
        assertEquals("pnr", res.getPerson().getPostnummer());
        assertEquals("port", res.getPerson().getPostort());

        verify(mockMonitoringService).logPULookup(personnummer, "FOUND");
    }

    @Test
    public void testGetPersonuppgifterSekretess() {
        Personnummer personnummer = createPnr("19121212-1212");

        when(puService.getPerson(any(Personnummer.class))).thenReturn(new PersonSvar(
                new Person(personnummer, true, false, "fnamn", "mnamn", "enamn", "paddr", "pnr", "port"), PersonSvar.Status.FOUND));

        Response response = personCtrl.getPersonuppgifter(personnummer.getPersonnummer());

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        PersonuppgifterResponse res = (PersonuppgifterResponse) response.getEntity();
        assertEquals(PersonSvar.Status.FOUND, res.getStatus());
        assertEquals(true, res.getPerson().isSekretessmarkering());
        assertEquals("fnamn", res.getPerson().getFornamn());
        assertEquals("mnamn", res.getPerson().getMellannamn());
        assertEquals("enamn", res.getPerson().getEfternamn());
        assertEquals("paddr", res.getPerson().getPostadress());
        assertEquals("pnr", res.getPerson().getPostnummer());
        assertEquals("port", res.getPerson().getPostort());

        verify(mockMonitoringService).logPULookup(personnummer, "FOUND");
    }

    @Test
    public void testGetPersonuppgifterMissingPerson() {
        Personnummer personnummer = createPnr("19010101-0101");

        when(puService.getPerson(any(Personnummer.class))).thenReturn(new PersonSvar(null, PersonSvar.Status.NOT_FOUND));

        Response response = personCtrl.getPersonuppgifter(personnummer.getPersonnummer());

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        PersonuppgifterResponse res = (PersonuppgifterResponse) response.getEntity();
        assertEquals(PersonSvar.Status.NOT_FOUND, res.getStatus());
        assertNull(res.getPerson());

        verify(mockMonitoringService).logPULookup(personnummer, "NOT_FOUND");
    }

    private Personnummer createPnr(String personId) {
        return Personnummer.createValidatedPersonnummer(personId)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer: " + personId));
    }

}
