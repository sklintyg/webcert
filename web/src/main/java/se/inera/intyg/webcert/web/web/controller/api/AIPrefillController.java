package se.inera.intyg.webcert.web.web.controller.api;

import io.swagger.annotations.Api;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;

@Path(AIPrefillController.PREFILL_STATUS_REQUEST_MAPPING)
@Api(value = "prefill-status-check", description = "Status för AIPrefill", produces = MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Slf4j
public class AIPrefillController extends AbstractApiController {

    protected static final String UTF_8_CHARSET = ";charset=utf-8";
    public static final String PREFILL_STATUS_REQUEST_MAPPING = "/prefill";
    private final Cache redisCacheLaunchId;


    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "prefill-get-prefill-status", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getPrefillStatus() {
        if (getWebCertUserService().getUser().getParameters() == null) {
            return Response.ok(Map.of("status", "complete")).build();
        }

        final var launchId = getWebCertUserService().getUser().getParameters().getLaunchId();

        if (launchId == null || launchId.isBlank()) {
            return Response.ok(Map.of("status", "complete")).build();
        }

        final var redisCacheKey = "prefillInProgress:" + launchId;
        final var prefillInProgress = redisCacheLaunchId.get(redisCacheKey);

        String status;
        if (prefillInProgress == null) {
            status = "complete";
        } else {
            if (Objects.equals(prefillInProgress.get(), "prefillInProgress")) {
                status = "loading";
            } else {
                status = "complete";
            }
        }
        return Response.ok(Map.of("status", status)).build();
    }
}
