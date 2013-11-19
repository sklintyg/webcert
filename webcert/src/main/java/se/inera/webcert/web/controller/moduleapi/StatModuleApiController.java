package se.inera.webcert.web.controller.moduleapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.service.FragaSvarService;
import se.inera.webcert.service.IntygService;
import se.inera.webcert.web.controller.moduleapi.dto.StatEntry;
import se.inera.webcert.web.controller.moduleapi.dto.StatRequestResponse;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * @author marced
 */
public class StatModuleApiController {

    @Autowired
    private IntygService intygService;

    @Autowired
    private FragaSvarService fragaSvarService;

    @Autowired
    private WebCertUserService webCertUserService;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getStats() {

        // TODO: get actual number for from service(s)
        StatEntry userStat = new StatEntry(0, 0);

        WebCertUser user = webCertUserService.getWebCertUser();

        long unhandledQuestions = fragaSvarService.getUnhandledFragaSvarForUnitsCount(user.getVardenheterIds());
        // TODO: get actual unsigned certs for unit numbers from service(s) when its implemented
        StatEntry unitStat = new StatEntry(0, unhandledQuestions);
        StatRequestResponse stats = new StatRequestResponse(unitStat, userStat);
        return Response.ok(stats).build();
    }
}
