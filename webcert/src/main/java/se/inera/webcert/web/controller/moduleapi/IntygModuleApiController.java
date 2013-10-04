package se.inera.webcert.web.controller.moduleapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.service.FragaSvarService;

/**
 * @author andreaskaltenbach
 */
public class IntygModuleApiController {

    @Autowired
    private FragaSvarService fragaSvarService;

    @GET
    @Path("/{intygId}/fragasvar")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<FragaSvar> fragaSvarForIntyg(@PathParam("intygId") String intygId) {
        return fragaSvarService.getFragaSvar(intygId);
    }

    @GET
    @Path("/{intygId}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<FragaSvar> intyg(@PathParam("intygId") String intygId) {

        // fetch intyg from intygstjanst



        return fragaSvarService.getFragaSvar(intygId);
    }
}
