package se.inera.webcert.web.controller.api;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.webcert.web.controller.api.dto.PersonuppgifterResponse;

import javax.ws.rs.core.Response;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static se.inera.webcert.web.controller.api.dto.PersonuppgifterResponse.Status.FOUND;
import static se.inera.webcert.web.controller.api.dto.PersonuppgifterResponse.Status.NOT_FOUND;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class PersonApiControllerTest {

    private PersonApiController personCtrl = new PersonApiController();

    @Test
    public void testGetPersonuppgifter() {

        Response response = personCtrl.getPersonuppgifter("19121212-1212");

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        PersonuppgifterResponse res = (PersonuppgifterResponse) response.getEntity();
        assertEquals(FOUND, res.getStatus());
        assertNotNull(res.getPersonuppgifter());
        assertNotNull(res.getPersonuppgifter().getFornamn());
        assertNotNull(res.getPersonuppgifter().getEfternamn());
        assertNotNull(res.getPersonuppgifter().getAdress());
    }

    @Test
    public void testGetPersonuppgifterMissingPerson() {

        Response response = personCtrl.getPersonuppgifter("18121212-1212");

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        PersonuppgifterResponse res = (PersonuppgifterResponse) response.getEntity();
        assertEquals(NOT_FOUND, res.getStatus());
        assertNull(res.getPersonuppgifter());
    }
}
