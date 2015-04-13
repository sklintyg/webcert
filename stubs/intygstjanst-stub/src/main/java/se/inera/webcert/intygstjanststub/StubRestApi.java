package se.inera.webcert.intygstjanststub;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

import se.inera.webcert.intygstjanststub.mode.StubMode;
import se.inera.webcert.intygstjanststub.mode.StubModeSingleton;
/**
 * @author marced
 */
public class StubRestApi {

    @Autowired
    private IntygStore intygStore;

    @GET
    @Path("/intyg")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<GetCertificateForCareResponseType> getAllIntyg() {
        return intygStore.getAllIntyg().values();
    }

    @GET
    @Path("/intyg/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public GetCertificateForCareResponseType getIntyg(@PathParam("id") String id) {
        return intygStore.getAllIntyg().get(id);
    }

    /**
     * Sets the @{StubMode} of this stub.
     *
     * @param mode ONLINE or OFFLINE
     * @return 204 No Content if OK, 400 Bad Request if fail.
     */
    @PUT
    @Path("/mode/{mode}")
    public Response setStubMode(@PathParam("mode") String mode) {
        try {
            StubMode requestedStubMode = StubMode.valueOf(mode);
            StubModeSingleton.getInstance().setStubMode(requestedStubMode);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid stub mode requested: '" + mode + ". Allowed values are '" + StubMode.ONLINE.name()
                            + "' and '" + StubMode.OFFLINE.name() + "'")
                    .build();
        }
    }

}
