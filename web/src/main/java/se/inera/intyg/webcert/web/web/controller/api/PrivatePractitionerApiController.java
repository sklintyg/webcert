package se.inera.intyg.webcert.web.web.controller.api;

import io.swagger.annotations.Api;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.context.annotation.Profile;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.privatepractitioner.PrivatePractitionerService;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.HospInformationResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerConfigResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerDetails;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerResponse;

@Api(value = "private-practitioner", produces = MediaType.APPLICATION_JSON)
@Profile("private-practitioner-service-active")
public class PrivatePractitionerApiController {

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private PrivatePractitionerService service;

    public PrivatePractitionerApiController(PrivatePractitionerService service) {
        this.service = service;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "register-private-practitioner", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response registerPractitioner(PrivatePractitionerDetails registerPrivatePractitionerRequest) {
        service.registerPrivatePractitioner(registerPrivatePractitionerRequest);
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "get-private-practitioner", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public PrivatePractitionerResponse getPrivatePractitioner() {
        return service.getLoggedInPrivatePractitioner();
    }

    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "get-private-practitioner-config", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public PrivatePractitionerConfigResponse getPrivatePractitionerConfig() {
        return service.getPrivatePractitionerConfig();
    }

    @GET
    @Path("/hospInformation")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "get-private-practitioner-hosp-information", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public HospInformationResponse getHospInformation() {
        return service.getHospInformation();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "update-private-practitioner", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public PrivatePractitionerResponse updatePrivatePractitioner(PrivatePractitionerDetails updatePrivatePractitionerRequest) {
        return service.editPrivatePractitioner(updatePrivatePractitionerRequest);
    }
}
