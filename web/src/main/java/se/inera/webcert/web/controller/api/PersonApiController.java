package se.inera.webcert.web.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.webcert.pu.model.Person;
import se.inera.webcert.pu.services.PUService;
import se.inera.webcert.web.controller.AbstractApiController;
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

    @Autowired
    private PUService puService;

    @GET
    @Path("/{personnummer}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getPersonuppgifter(@PathParam("personnummer") String personnummer) {

        LOG.debug("Hämtar personuppgifter för: {}", personnummer);

        Person person = puService.getPerson(personnummer);

        if (person != null) {
            return Response.ok(new PersonuppgifterResponse(FOUND, person)).build();
        } else {
            return Response.ok(new PersonuppgifterResponse(NOT_FOUND)).build();
        }
    }
}
