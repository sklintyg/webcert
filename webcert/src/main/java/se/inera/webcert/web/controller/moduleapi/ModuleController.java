package se.inera.webcert.web.controller.moduleapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.webcert.web.controller.AbstractApiController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by magnus on 2014-03-06.
 */
@Component
public class ModuleController extends AbstractApiController {
    @Autowired
    private List<ModuleEntryPoint> moduleEntryPoints;

    @GET
    @Path("/modulesMap")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getModulesMap() {

        Map<String, String> modules = new HashMap();
        for (ModuleEntryPoint entryPoint : moduleEntryPoints) {
            modules.put(entryPoint.getModuleName(), entryPoint.getModuleScriptPath());
        }

        return Response.ok(modules).build();
    }
}
