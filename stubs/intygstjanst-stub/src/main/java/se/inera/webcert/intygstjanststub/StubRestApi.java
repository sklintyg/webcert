package se.inera.webcert.intygstjanststub;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

import se.inera.webcert.intygstjanststub.mode.StubMode;
import se.inera.webcert.intygstjanststub.mode.StubModeSingleton;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;

/**
 * This REST API is used to introspect stub state and modify its characteristics during runtime, e.g. put it into
 * offline mode or setting of artifical latencies.
 *
 * @author marced
 */
public class StubRestApi {

    @Autowired
    private IntygStore intygStore;

    @Autowired
    private BootstrapBean intygstjanstStubBootstrapBean;

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
        GetCertificateForCareResponseType resp = intygStore.getAllIntyg().get(id);
        ResultType resultType = new ResultType();
        if (resp == null) {
            resp = new GetCertificateForCareResponseType();

            resultType.setResultCode(ResultCodeType.ERROR);
            resultType.setErrorId(ErrorIdType.APPLICATION_ERROR);
            resultType.setResultText("Intyg " + id + " does not exist in stub. At least not yet.");
            resp.setResult(resultType);
            return resp;
        }
        resultType.setResultCode(ResultCodeType.OK);
        resp.setResult(resultType);
        return resp;
    }

    @DELETE
    @Path("/intyg")
    public Response resetIntygStore() {
        intygStore.clear();
        intygstjanstStubBootstrapBean.initData();
        return Response.noContent().build();
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

    /**
     * Sets the artifical latency of the stub for serving annotated requests.
     *
     * @param millis 0 to Long.MAX_VALUE
     * @return 204 No Content if OK, 400 Bad Request if fail.
     */
    @PUT
    @Path("/latency/{millis}")
    public Response setStubMode(@PathParam("millis") Long millis) {
        try {
            if(millis < 0L || millis > Long.MAX_VALUE) {
                throw new IllegalArgumentException();
            }
            StubModeSingleton.getInstance().setLatency(millis);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid stub latency requested: '" + millis + ". Allowed values are 0 to " + Long.MAX_VALUE)
                    .build();
        }
    }

}
