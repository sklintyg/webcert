package se.inera.webcert.web.controller.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.webcert.pu.model.Person;
import se.inera.webcert.pu.model.PersonSvar;
import se.inera.webcert.pu.services.PUService;
import se.inera.webcert.service.monitoring.MonitoringLogService;
import se.inera.webcert.web.controller.api.dto.PersonuppgifterResponse;

import javax.ws.rs.core.Response;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

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
        String personnummer = "19121212-1212";

        when(puService.getPerson(anyString())).thenReturn(
                new PersonSvar(new Person(personnummer, "fnamn", "mnamn", "enamn", "paddr", "pnr", "port"), PersonSvar.Status.FOUND));

        Response response = personCtrl.getPersonuppgifter(personnummer);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        PersonuppgifterResponse res = (PersonuppgifterResponse) response.getEntity();
        assertEquals(PersonSvar.Status.FOUND, res.getStatus());
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

        String personnummer = "18121212-1212";

        when(puService.getPerson(anyString())).thenReturn(new PersonSvar(null, PersonSvar.Status.NOT_FOUND));

        Response response = personCtrl.getPersonuppgifter(personnummer);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        PersonuppgifterResponse res = (PersonuppgifterResponse) response.getEntity();
        assertEquals(PersonSvar.Status.NOT_FOUND, res.getStatus());
        assertNull(res.getPerson());

        verify(mockMonitoringService).logPULookup(personnummer, "NOT_FOUND");
    }
}
