package se.inera.webcert.web.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.webcert.web.controller.AbstractApiController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Controller managing module wiring.
 */
@Path("/modules")
public class ModuleApiController extends AbstractApiController {

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    /**
     * Serving module configuration for Angular bootstrapping.
     *
     * @return a JSON object
     */
    @GET
    @Path("/map")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getModulesMap() {
        return Response.ok(moduleRegistry.listAllModules()).build();
    }
}
