package se.inera.intyg.webcert.web.web.controller.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.dto.InvalidateRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(InvalidateLaunchIdController.SESSION_STATUS_REQUEST_MAPPING)
@Api(value = "session-validate", description = "REST API för sessionen som kontrollerar localId", produces = MediaType.APPLICATION_JSON)
public class InvalidateLaunchIdController {
    @Autowired
    private InvalidateRequestService invalidateRequestService;
    @Autowired
    private WebCertUserService user;
    public static final String SESSION_STATUS_REQUEST_MAPPING = "/v1/session";
    public static final String SESSION_INVALIDATE = "/invalidate";
    protected static final String UTF_8_CHARSET = ";charset=utf-8";


    @POST
    @Path(SESSION_INVALIDATE)
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getLaunchIdValidation(InvalidateRequest invalidateRequest) {

        if (invalidateRequest.validateRequest()) {
            final var reqHsaId = invalidateRequest.getUserHsaId();
            final var reqLaunchId = invalidateRequest.getLaunchId();
            final var userLaunchId = invalidateRequestService.getLaunchIdStoredInRedis(reqLaunchId);

            if (reqLaunchId.equals(userLaunchId)) {

                String hsaIdStoredInSession = invalidateRequestService.getHsaIdFromRedisSession(reqLaunchId);

                if (hsaIdStoredInSession.equals(reqHsaId)) {
                    invalidateRequestService.invalidateSession(reqLaunchId);
                }
            }
        }
        return Response.noContent().build();
    }
}
