package se.inera.intyg.webcert.web.web.controller.facade;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.facade.UserService;

@Path("/user")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getUser() {
        LOG.debug("Getting logged in user");
        final var loggedInUser = userService.getLoggedInUser();
        return Response.ok(loggedInUser).build();
    }
}
