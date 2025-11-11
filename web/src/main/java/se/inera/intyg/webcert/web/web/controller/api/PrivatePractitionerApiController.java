package se.inera.intyg.webcert.web.web.controller.api;

import io.swagger.annotations.Api;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.privatepractitioner.PrivatePractitionerService;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.HospInformationResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerConfigResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerRegistrationRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.RegisterPrivatePractitionerResponse;

@Path("/private-practitioner")
@Api(value = "private-practitioner", produces = MediaType.APPLICATION_JSON)
@Profile("private-practitioner-service-active")
@RequiredArgsConstructor
public class PrivatePractitionerApiController {

    PrivatePractitionerService service;

    @POST
    @PerformanceLogging(eventAction = "register-private-practitioner", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public RegisterPrivatePractitionerResponse registerPractitioner(
        PrivatePractitionerRegistrationRequest registerPrivatePractitionerRequest) {
        final var privatePractitioner = service.registerPrivatePractitioner(registerPrivatePractitionerRequest);
        return RegisterPrivatePractitionerResponse.builder()
            .privatePractitioner(privatePractitioner)
            .build();
    }

    @GET
    public RegisterPrivatePractitionerResponse getPrivatePractitioner() {
        return RegisterPrivatePractitionerResponse.builder()
            .privatePractitioner(service.getPrivatePractitioner())
            .build();
    }

    @PUT
    public Response updatePrivatePractitioner(PrivatePractitionerDTO privatePractitionerDTO) {
        service.updatePrivatePractitioner(privatePractitionerDTO);
        return Response.ok().build();
    }


    @GET
    @Path("/config")
    public PrivatePractitionerConfigResponse getPrivatePractitionerConfig() {
        return service.getPrivatePractitionerConfig();
    }

    @GET
    @Path("/hospInformation")
    public HospInformationResponse getHospInformation() {
        return service.getHospInformation();
    }

}
