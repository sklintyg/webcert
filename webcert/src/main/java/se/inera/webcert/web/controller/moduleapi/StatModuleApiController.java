package se.inera.webcert.web.controller.moduleapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.service.IntygService;
import se.inera.webcert.web.controller.moduleapi.dto.StatEntry;
import se.inera.webcert.web.controller.moduleapi.dto.StatRequestResponse;

/**
 * @author marced
 */
public class StatModuleApiController {

    @Autowired
    private IntygService intygService;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response intyg(@PathParam("intygId") String intygId) {
        StatEntry unitStat = new StatEntry((int) (Math.random() *10),(int) (Math.random() *30));
        StatEntry userStat = new StatEntry((int) (Math.random() *5),(int) (Math.random() *10));
        
        StatRequestResponse stats = new StatRequestResponse(unitStat,userStat);
        //TODO: get actual numbers from service(s)
        return Response.ok(stats).build();
    }
}
