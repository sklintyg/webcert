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
import se.inera.intyg.webcert.web.web.controller.api.dto.HospInformationResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.PrivatePractitionerConfigResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.PrivatePractitionerDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.PrivatePractitionerResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.PrivatePractitionerRegisterRequest;

@Path("/private-practitioner")
@Api(value = "private-practitioner", description = "REST API för private practitioner", produces = MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class PrivatePractitionerApiController {

  PrivatePractitionerService service;
    
    @POST
    public PrivatePractitionerResponse registerPractitioner(
        PrivatePractitionerRegisterRequest registerPrivatePractitionerRequest) {
        final var privatePractitioner = service.registerPrivatePractitioner(registerPrivatePractitionerRequest);
        return PrivatePractitionerResponse.builder()
            .privatePractitioner(PrivatePractitionerDTO.create(privatePractitioner))
            .build();
    }

    @GET
    public PrivatePractitionerResponse getPrivatePractitioner() {
        return PrivatePractitionerResponse.builder()
            .privatePractitioner(PrivatePractitionerDTO.create(service.getPrivatePractitioner()))
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
        return PrivatePractitionerConfigResponse.builder()
            .getPrivatePractitionerConfig(service.getPrivatePractitionerConfig())
            .build();
    }

    @GET
    @Path("/hospInformation")
    public HospInformationResponse getHospInformation() {
        return HospInformationResponse.builder()
            .hospInformation(service.getHospInformation())
            .build();
    }

}
