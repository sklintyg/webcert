package se.inera.intyg.webcert.web.web.controller.testability;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.integration.fmb.services.FmbService;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.swagger.annotations.Api;

@Api(value = "testability fmb", description = "REST API för testbarhet - FMB")
@Path("/fmb")
public class FmbResource {

    @Autowired
    private FmbService fmbService;

    /**
     * Populate FMB data using the configured endpoint. Using a GET to update data might
     * not be recommended. However, it is a very convenient way to populate FMB data from
     * the browser without waiting for the automatic population that happens once each
     * day. It is also the only way I could figure out to invoke it from the browser
     * session in the Fitnesse tests.
     */
    @GET
    @Path("/updatefmbdata")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonPropertyDescription("Update FMB data")
    public Response updateFmbData() {
        fmbService.updateData();
        return Response.ok().build();
    }

}
