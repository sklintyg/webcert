package se.inera.intyg.webcert.web.web.controller.api;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Controller that logs messages from JavaScript to the normal log.
 */
@Path("/jslog")
@Api(value = "jslog", description = "REST API för loggning från frontend till backend-log", produces = MediaType.APPLICATION_JSON)
public class JsLogApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(JsLogApiController.class);

    @POST
    @Path("/debug")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response debug(String message) {
        LOG.debug(message);
        return Response.ok().build();
    }
}
