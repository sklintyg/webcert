package se.inera.webcert.web.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.webcert.web.controller.AbstractApiController;
import se.inera.webcert.web.controller.api.dto.Personuppgifter;
import se.inera.webcert.web.controller.api.dto.PersonuppgifterResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static se.inera.webcert.web.controller.api.dto.PersonuppgifterResponse.Status.FOUND;
import static se.inera.webcert.web.controller.api.dto.PersonuppgifterResponse.Status.NOT_FOUND;

@Path("/person")
public class PersonApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(PersonApiController.class);

    @GET
    @Path("/{personnummer}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getPersonuppgifter(@PathParam("personnummer") String personnummer) {

        LOG.debug("Hämtar personuppgifter för: {}", personnummer);

        // TODO: Hämta information från PU-tjänsten.
        if ("19121212-1212".equals(personnummer) || "20121212-1212".equals(personnummer)) {

            Personuppgifter personuppgifter = new Personuppgifter();
            personuppgifter.setPersonnummer(personnummer);
            personuppgifter.setFornamn("Test");
            personuppgifter.setEfternamn("Testsson");
            personuppgifter.setAdress("Storgatan 23");

            return Response.ok(new PersonuppgifterResponse(FOUND, personuppgifter)).build();
        } else {
            return Response.ok(new PersonuppgifterResponse(NOT_FOUND)).build();
        }
    }
}
