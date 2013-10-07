package se.inera.webcert.web.controller.moduleapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.service.IntygService;

/**
 * @author andreaskaltenbach
 */
public class IntygModuleApiController {

    @Autowired
    private IntygService intygService;

    @GET
    @Path("/{intygId}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response intyg(@PathParam("intygId") String intygId) {
        return Response.ok(intygService.fetchIntygData(intygId)).build();
    }
}
