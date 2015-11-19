package se.inera.intyg.webcert.intygstjanststub;

import java.util.Collection;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.intyg.webcert.intygstjanststub.mode.StubMode;
import se.inera.intyg.webcert.intygstjanststub.mode.StubModeSingleton;
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

    private static final Logger LOG = LoggerFactory.getLogger(StubRestApi.class);

    @Autowired
    private IntygStore intygStore;

    @Autowired(required = false)
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
        if (intygstjanstStubBootstrapBean != null) {
            intygstjanstStubBootstrapBean.initData();
        } else {
            LOG.warn("Could not reset intyg stub store. Bootstrap bean not available in the current spring profile. (E.g. dev or dev,wc-all-stubs or wc-it-stub required)");
        }
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
            if (millis < 0L || millis > Long.MAX_VALUE) {
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
