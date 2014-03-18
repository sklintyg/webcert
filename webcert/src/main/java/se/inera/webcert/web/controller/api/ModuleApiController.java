package se.inera.webcert.web.controller.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.modules.registry.IntygModule;
import se.inera.webcert.modules.registry.IntygModuleRegistry;
import se.inera.webcert.web.controller.AbstractApiController;

/**
 * Controller managing module wiring
 */
@Path("/modules")
public class ModuleApiController extends AbstractApiController {

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    /**
     * Serving module configuration for Angular bootstrapping
     * @return a JSON object
     */
    @GET
    @Path("/map")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getModulesMap() {
        List<IntygModule> modules = moduleRegistry.listAllModules();
        return Response.ok(modules).build();
    }
}
