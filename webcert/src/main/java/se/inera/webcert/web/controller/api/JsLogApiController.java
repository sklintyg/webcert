package se.inera.webcert.web.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Controller that logs messages from JavaScript to the normal log.
 */
@Path("/jslog")
public class JsLogApiController {

    protected static final String UTF_8_CHARSET = ";charset=utf-8";

    private static final Logger LOG = LoggerFactory.getLogger(JsLogApiController.class);

    @POST
    @Path("/debug")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response debug(String message) {
        LOG.debug(message);
        return Response.ok().build();
    }
}
