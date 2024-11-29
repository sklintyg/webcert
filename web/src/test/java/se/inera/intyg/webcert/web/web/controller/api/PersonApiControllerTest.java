/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.infra.pu.integration.api.model.Person;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.pu.integration.api.services.PUService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.api.dto.PersonuppgifterResponse;

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

        when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.found(
            new Person(personnummer, false, false, "fnamn", "mnamn", "enamn", "paddr", "pnr", "port")));

        Response response = personCtrl.getPersonuppgifter(personnummer.getPersonnummer());

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        PersonuppgifterResponse res = (PersonuppgifterResponse) response.getEntity();
        assertEquals(PersonSvar.Status.FOUND, res.getStatus());
        assertEquals(false, res.getPerson().sekretessmarkering());
        assertEquals("fnamn", res.getPerson().fornamn());
        assertEquals("mnamn", res.getPerson().mellannamn());
        assertEquals("enamn", res.getPerson().efternamn());
        assertEquals("paddr", res.getPerson().postadress());
        assertEquals("pnr", res.getPerson().postnummer());
        assertEquals("port", res.getPerson().postort());

        verify(mockMonitoringService).logPULookup(personnummer, "FOUND");
    }

    @Test
    public void testGetPersonuppgifterSekretess() {
        Personnummer personnummer = createPnr("19121212-1212");

        when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.found(
            new Person(personnummer, true, false, "fnamn", "mnamn", "enamn", "paddr", "pnr", "port")));

        Response response = personCtrl.getPersonuppgifter(personnummer.getPersonnummer());

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        PersonuppgifterResponse res = (PersonuppgifterResponse) response.getEntity();
        assertEquals(PersonSvar.Status.FOUND, res.getStatus());
        assertEquals(true, res.getPerson().sekretessmarkering());
        assertEquals("fnamn", res.getPerson().fornamn());
        assertEquals("mnamn", res.getPerson().mellannamn());
        assertEquals("enamn", res.getPerson().efternamn());
        assertEquals("paddr", res.getPerson().postadress());
        assertEquals("pnr", res.getPerson().postnummer());
        assertEquals("port", res.getPerson().postort());

        verify(mockMonitoringService).logPULookup(personnummer, "FOUND");
    }

    @Test
    public void testGetPersonuppgifterMissingPerson() {
        Personnummer personnummer = createPnr("19010101-0101");

        when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.notFound());

        Response response = personCtrl.getPersonuppgifter(personnummer.getPersonnummer());

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        PersonuppgifterResponse res = (PersonuppgifterResponse) response.getEntity();
        assertEquals(PersonSvar.Status.NOT_FOUND, res.getStatus());
        assertNull(res.getPerson());

        verify(mockMonitoringService).logPULookup(personnummer, "NOT_FOUND");
    }

    private Personnummer createPnr(String personId) {
        return Personnummer.createPersonnummer(personId)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer: " + personId));
    }

}