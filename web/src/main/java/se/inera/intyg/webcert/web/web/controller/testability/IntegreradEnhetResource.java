package se.inera.intyg.webcert.web.web.controller.testability;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Testbarhetsresurs för att till och börja med radera identifierade integrerade vårdenheter.
 */
@Service
@Api(value = "testability integreradevardenheter", description = "REST API för testbarhet - Integrerade vårdenheter")
@Path("/integreradevardenheter")
public class IntegreradEnhetResource {

    private static final int OK = 200;
    private static final int BAD_REQUEST = 400;

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @DELETE
    @Path("/{hsaId}")
    @ApiResponses(value = {
            @ApiResponse(code = OK, message = "Given identified integrated unit was deleted"),
            @ApiResponse(code = BAD_REQUEST, message = "If supplied hsaId was null or blank")
    })
    public Response deleteIntegreradVardenhet(@PathParam("hsaId") String hsaId) {
        if (isNullOrEmpty(hsaId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Specified hsaId is null or blank").build();
        }

        integreradeEnheterRegistry.deleteIntegreradEnhet(hsaId);
        return Response.ok().build();
    }

    private boolean isNullOrEmpty(@PathParam("hsaId") String hsaId) {
        return hsaId == null || hsaId.trim().length() == 0;
    }
}
