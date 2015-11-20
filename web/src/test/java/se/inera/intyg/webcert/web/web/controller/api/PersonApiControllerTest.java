package se.inera.intyg.webcert.web.web.controller.api;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.modules.support.api.dto.Personnummer;
import se.inera.webcert.pu.model.Person;
import se.inera.webcert.pu.model.PersonSvar;
import se.inera.webcert.pu.services.PUService;
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
        Personnummer personnummer = new Personnummer("19121212-1212");

        when(puService.getPerson(any(Personnummer.class))).thenReturn(
                new PersonSvar(new Person(personnummer, false, "fnamn", "mnamn", "enamn", "paddr", "pnr", "port"), PersonSvar.Status.FOUND));

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
        Personnummer personnummer = new Personnummer("19121212-1212");

        when(puService.getPerson(any(Personnummer.class))).thenReturn(
                new PersonSvar(new Person(personnummer, true, "fnamn", "mnamn", "enamn", "paddr", "pnr", "port"), PersonSvar.Status.FOUND));

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
        Personnummer personnummer = new Personnummer("18121212-1212");

        when(puService.getPerson(any(Personnummer.class))).thenReturn(new PersonSvar(null, PersonSvar.Status.NOT_FOUND));

        Response response = personCtrl.getPersonuppgifter(personnummer.getPersonnummer());

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        PersonuppgifterResponse res = (PersonuppgifterResponse) response.getEntity();
        assertEquals(PersonSvar.Status.NOT_FOUND, res.getStatus());
        assertNull(res.getPerson());

        verify(mockMonitoringService).logPULookup(personnummer, "NOT_FOUND");
    }
}
