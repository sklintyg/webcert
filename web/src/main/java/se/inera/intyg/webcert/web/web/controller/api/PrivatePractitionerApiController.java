package se.inera.intyg.webcert.web.web.controller.api;

import io.swagger.annotations.Api;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import se.inera.intyg.webcert.web.ppsintegration.PrivatePractitionerService;
import se.inera.intyg.webcert.web.web.controller.api.dto.PrivatePractitionerDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.PrivatePractitionerResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.RegisterPrivatePractitionerRequest;

@Path("/private-practitioner")
@Api(value = "private-practitioner", description = "REST API för private practitioner", produces = MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class PrivatePractitionerApiController {

  PrivatePractitionerService service;
    
    @POST
    public PrivatePractitionerResponse registerPractitioner(RegisterPrivatePractitionerRequest registerPrivatePractitionerRequest) {
        final var privatePractitioner = service.registerPrivatePractitioner(registerPrivatePractitionerRequest);
        return PrivatePractitionerResponse.builder()
            .privatePractitioner(PrivatePractitionerDTO.create(privatePractitioner))
            .build();
    }

    @GET
    public Response getPrivatePractitioner() {
        final var privatePractitioner = service.getPrivatePractitioner();
        return Response.ok(privatePractitioner).build();
    }

    @PUT
    public Response updatePrivatePractitioner(PrivatePractitionerDTO privatePractitionerDTO) {
        service.updatePrivatePractitioner(privatePractitionerDTO);
        return Response.ok().build();
    }


    @GET
    @Path("/config")
    public Response getPrivatePractitionerConfig() {
        final var config = service.getPrivatePractitionerConfig();

        return Response.ok(config).build();
    }

    @GET
    @Path("/hospInformation")
    public Response getHospInformation() {
        return Response.ok(service.getHospInformation()).build();
    }

}
